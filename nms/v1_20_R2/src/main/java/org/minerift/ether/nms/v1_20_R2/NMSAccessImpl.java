package org.minerift.ether.nms.v1_20_R2;

import com.mojang.serialization.Dynamic;
import io.netty.buffer.Unpooled;
import net.kyori.adventure.text.Component;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_20_R2.CraftWorld;
import org.minerift.ether.Ether;
import org.minerift.ether.config.ConfigType;
import org.minerift.ether.config.main.MainConfig;
import org.minerift.ether.debug.Experimental;
import org.minerift.ether.debug.Experiments;
import org.minerift.ether.debug.NeedsTesting;
import org.minerift.ether.dimension.Dimension;
import org.minerift.ether.nms.NMSAccess;
import org.minerift.ether.nms.v1_20_R2.data.AttributeRegistry;
import org.minerift.ether.nms.world.Chunk;
import org.minerift.ether.nms.world.ChunkGetter;
import org.minerift.ether.util.BukkitUtils;
import org.minerift.ether.util.nbt.tags.Tag;
import org.minerift.ether.util.reflect.Reflect;
import org.minerift.ether.util.reflect.ReflectedObject;
import org.minerift.ether.world.EntityArchetype;
import org.minerift.ether.world.EntityLoadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class NMSAccessImpl implements NMSAccess {

    private static final Logger LOGGER = LoggerFactory.getLogger(NMSAccessImpl.class);

    private final RegistryAccessImpl registryAccess;
    private final AttributeRegistry attrRegistry;
    private final ExperimentsImpl exp;
    private Map<ResourceLocation, Dimension> dimLookup;

    public NMSAccessImpl() {
        this.registryAccess = new RegistryAccessImpl();
        this.attrRegistry = AttributeRegistry.access();
        this.exp = new ExperimentsImpl();
        this.dimLookup = new HashMap<>();
        // iterate through cfg dims and check if they exist; err log if they are invalid

        // testing reflection mapping
        ReflectionMappings.class.getClass(); // load class and fields for reflection
        System.out.println("frozen registry obfuscated field: " + ReflectionMappings.FROZEN_REGISTRY_FIELD_NAME);
        ReflectedObject<Registry<Biome>> refBiomeRegistry = Reflect.of(MinecraftServer.getServer().registryAccess().registryOrThrow(Registries.BIOME));
        System.out.println("Biome Registry Frozen? " + (boolean) refBiomeRegistry.readField(ReflectionMappings.FROZEN_REGISTRY_FIELD_NAME));
    }

    @Override
    public <T extends Tag> T fixUpItemName(T nbt, int dataVersion) {
        net.minecraft.nbt.Tag nativeNbt = getConverter().tryAsNativeTag(nbt);
        Dynamic<net.minecraft.nbt.Tag> name = new Dynamic<>(NbtOps.INSTANCE, nativeNbt);
        Dynamic<net.minecraft.nbt.Tag> converted = DataFixers.getDataFixer().update(References.ITEM_NAME, name, dataVersion, getDataVersion());
        if(name.equals(converted)) {
            converted = DataFixers.getDataFixer().update(References.BLOCK_NAME, name, dataVersion, getDataVersion());
        }

        return (T) getConverter().asTag(converted.cast(NbtOps.INSTANCE));
    }

    // TODO: test this after finishing NbtFixerOps.class
    // TODO: refactor to move out of NMS into core
    /*@Override
    public <T extends Tag> T fixUpItemName(T nbt, int dataVersion) {
        Dynamic<Tag> name = new Dynamic<>(NbtFixerOps.INSTANCE, nbt);
        Dynamic<Tag> converted = DataFixers.getDataFixer().update(References.ITEM_NAME, name, dataVersion, getDataVersion());
        if(name.equals(converted)) {
            converted = DataFixers.getDataFixer().update(References.BLOCK_NAME, name, dataVersion, getDataVersion());
        }

        return (T) converted.cast(NbtFixerOps.INSTANCE);
    }*/

    @NeedsTesting
    @Override
    public void addEntity(World world, EntityArchetype entity) throws EntityLoadException {
        ServerLevel level = getConverter().asNativeWorld(world);
        CompoundTag nativeTag = (CompoundTag) getConverter().tryAsNativeTag(entity.getNbtData());
        Entity worldEntity = EntityType.loadEntityRecursive(nativeTag, level, (entity1) -> {
            entity1.moveTo(entity.getLocation().getXd(), entity.getLocation().getYd(), entity.getLocation().getZd());
            return entity1;
        });
        if(worldEntity == null) {
            throw new EntityLoadException("Entity archetype (" + entity.getId() + ") failed to load: invalid type");
        }
        if(!level.tryAddFreshEntityWithPassengers(worldEntity)) {
            throw new EntityLoadException("Entity failed to add to world: duplicate UUID " + worldEntity.getStringUUID());
        }
        world.getChunkAtAsync(BukkitUtils.asBukkitLocation(world, entity.getLocation())).thenAccept((_chunk) -> {
            LevelChunk chunk = getConverter().asChunk(_chunk).asNative();
            chunk.playerChunk.broadcast(new ClientboundAddEntityPacket(worldEntity), false);
            LOGGER.debug("Broadcast add entity packet");
        });
        LOGGER.debug("Added entity {} at {}", entity.getId(), entity.getLocation()); // debug
    }

    @Override
    public Dimension getDimFromWorld(World world) {
        MainConfig cfg = Ether.inst().getConfig(ConfigType.MAIN);
        ServerLevel lvl = ((CraftWorld)world).getHandle();
        String resLoc = lvl.dimension().location().toString();
        LOGGER.error(resLoc);
        return cfg.getDimensions().get(resLoc);
    }


    private void clearAllBlockEntities(LevelChunk chunk) {
        Set<BlockPos> pendingBlockEntities = chunk.getBlockEntitiesPos(); // contains pending block entities
        pendingBlockEntities.forEach(chunk::removeBlockEntity);
        chunk.clearAllBlockEntities(); // do rest of the work
    }


    // TODO: for biomes: let's do it manually as a configurable parameter
    //  - each time a chunk is cleared or an island pasted, a BiomeSource or BiomeProvider could be used
    //    to provide the ability to determine how to set the biomes; a simple one by default may exist with just
    //    simply setting the whole chunk to a specific biome (for icy islands, icy biomes, or desert islands, or
    //    other sources to describe biome setting.
    //  - the BiomeSource could also describe for the SchematicPasteOptions to read the biomes saved to the schematic
    //    and use those for more complex islands.
    @Override
    public void clearChunk(Chunk chunk, boolean clearEntities) { // assumes chunk is already loaded based on retrieval
        synchronized (chunk.asNative()) { // TODO: review
            final LevelChunk nChunk = (LevelChunk) chunk.asNative();
            final ServerLevel level = nChunk.level;
            final LevelChunk emptyChunk = new LevelChunk(level, nChunk.getPos());

            // TODO: for biomes:
            //  - use VarHandle to get biomes palette in each LevelChunkSection
            //  - write each section's biome palette to a buffer (or array of buffers)
            //  - once cleared, read each section's biome palette back
            //  - if this doesn't work, could add a biome parameter (in core, a Dimension class could contain the main biome)

            final ServerChunkCache serverChunkCache = level.getChunkSource();

            // Write empty chunk section to buffer
            final FriendlyByteBuf emptySectionBuf = new FriendlyByteBuf(Unpooled.buffer());
            emptyChunk.getSection(0).write(emptySectionBuf, null, 0);

            // TODO: clear block entities before clearing entities?

            if(clearEntities) {
                // TODO: switch to native impl for entities; schedule on main thread?
                /*ChunkEntitySlices entityStore = level.getEntityLookup().getChunk(chunk.getX(), chunk.getZ());
                if(entityStore != null) {
                    List<Entity> entities = new ArrayList<>(16);
                    entityStore.getEntities(null,
                            new AABB(new Vec3()));
                }*/
                Arrays.stream(level.getChunkEntities(chunk.getX(), chunk.getZ())) // this needs to be done sync
                        .filter(entity -> entity.getType() != org.bukkit.entity.EntityType.PLAYER)
                        .forEach(org.bukkit.entity.Entity::remove);
            }

            // Update chunk and sections
            clearAllBlockEntities(nChunk);
            for(LevelChunkSection section : nChunk.getSections()) {
                section.read(emptySectionBuf);
                section.recalcBlockCounts();
                emptySectionBuf.resetReaderIndex();
            }

            // Update heightmaps for chunk
            for(Heightmap.Types type : ChunkStatus.FULL.heightmapsAfter()) {
                nChunk.setHeightmap(type, emptyChunk.heightmaps.get(type).getRawData());
            }

            nChunk.setBlockEmptinessMap(emptyChunk.getBlockEmptinessMap());
            nChunk.setSkyEmptinessMap(emptyChunk.getSkyEmptinessMap());
            nChunk.setBlockNibbles(emptyChunk.getBlockNibbles());
            nChunk.setSkyNibbles(emptyChunk.getSkyNibbles());

            nChunk.setUnsaved(true);

            for(int i = 0; i < 4; i++) {
                for(int j = nChunk.getMinSection() * 4; j < nChunk.getMaxSection() * 4; j++) {
                    for(int k = 0; k < 4; k++) {
                        nChunk.setBiome(i, j, k, emptyChunk.getNoiseBiome(i, j, k));
                    }
                }
            }

            // Resend entire chunk packet
            ClientboundLevelChunkWithLightPacket packet = new ClientboundLevelChunkWithLightPacket(nChunk, serverChunkCache.getLightEngine(), null, null, true);
            nChunk.getChunkHolder().vanillaChunkHolder.broadcast(packet, false);
            System.out.println("Cleared chunk at " + chunk.getX() + ", " + chunk.getZ());
        }
    }

    @Experimental
    @Override
    public CompletableFuture<Void> clearChunks(ChunkGetter cg, Chunk e1, Chunk e2, boolean clearEntities) {
        // TODO: create new impl that iterates over chunk coords with CompletableFuture's
        //  for each chunk, then use CompletableFuture#allOf
        return clearChunksLogic(e1, e2, pos -> {
            cg.getChunkWCallback(e1.getWorld(), pos.x, pos.z, chunk -> {
                clearChunk(chunk, clearEntities); return chunk;
            });
        });
    }

    @Experimental
    @Override
    public CompletableFuture<Void> clearChunks(Chunk e1, Chunk e2, boolean clearEntities) {
        return clearChunks(ChunkGetter.SYNC, e1, e2, clearEntities);
    }

    private CompletableFuture<Void> clearChunksLogic(Chunk e1, Chunk e2, Consumer<ChunkPos> clearChunk) {
        if(e1.getWorld() != e2.getWorld()) {
            throw new IllegalArgumentException("Chunks are not in the same world");
        }

        final LevelChunk nChunk1 = (LevelChunk) e1.asNative();
        final LevelChunk nChunk2 = (LevelChunk) e2.asNative();

        final ServerLevel level = (ServerLevel) e1.getNativeWorld();
        //final ServerChunkCache serverChunkCache = level.getChunkSource();

        Bukkit.broadcast(Component.text("Clearing chunk contents..."));
        CompletableFuture<Void> res = CompletableFuture.runAsync(
                () -> ChunkPos.rangeClosed(nChunk1.getPos(), nChunk2.getPos()).forEach(clearChunk));

        //Bukkit.broadcast(Component.text("Relighting..."));
        //serverChunkCache.getLightEngine().relight(getNeighboringChunks(e1, e2), a -> {}, b -> {});

        Bukkit.broadcast(Component.text(String.format("Cleared %d chunk(s)",
                ChunkPos.rangeClosed(nChunk1.getPos(), nChunk2.getPos()).count())));
        return res;
    }

    @Override
    public void broadcastChunkBiomeUpdates(World world, List<Chunk> chunks) {
        // because native is LevelChunk, just cast to ChunkAccess (superclass)
        List<ChunkAccess> lcs = chunks.stream().map((c) -> (ChunkAccess)c.asNative()).toList();
        ((CraftWorld)world).getHandle().getChunkSource().chunkMap.resendBiomesForChunks(lcs);

        //((LevelChunk)null).level.getChunkSource().chunkMap.generator // TODO: really good step towards finding biome gen solution
    }

    @NeedsTesting
    @Override
    public int getDataVersion() { // FIXME
        return SharedConstants.getCurrentVersion().getDataVersion().getVersion();
    }

    @Override
    public RegistryAccessImpl registryAccess() {
        //((CraftItemStack)((ItemStack)null)).handle.
        return registryAccess;
    }

    public AttributeRegistry attrRegistry() {
        return AttributeRegistry.access();
    }

    @Override
    public Experiments experiments() {
        return exp;
    }

    @Override
    public NativeTypeConversionsImpl getConverter() {
        return NativeTypeConversionsImpl.inst();
    }
}
