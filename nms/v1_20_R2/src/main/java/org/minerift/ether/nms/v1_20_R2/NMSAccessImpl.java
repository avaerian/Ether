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
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.minerift.ether.debug.Experimental;
import org.minerift.ether.debug.Experiments;
import org.minerift.ether.debug.NeedsTesting;
import org.minerift.ether.nms.NMSAccess;
import org.minerift.ether.nms.v1_20_R2.data.AttributeRegistry;
import org.minerift.ether.nms.world.Chunk;
import org.minerift.ether.nms.world.ChunkGetter;
import org.minerift.ether.util.nbt.tags.Tag;
import org.minerift.ether.util.reflect.Reflect;
import org.minerift.ether.util.reflect.ReflectedObject;
import org.minerift.ether.world.EntityArchetype;
import org.minerift.ether.world.EntityLoadException;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Consumer;

public class NMSAccessImpl implements NMSAccess {

    private final RegistryAccessImpl registryAccess;
    private final AttributeRegistry attrRegistry;
    private final ExperimentsImpl exp;

    public NMSAccessImpl() {
        this.registryAccess = new RegistryAccessImpl();
        this.attrRegistry = AttributeRegistry.access();
        this.exp = new ExperimentsImpl();

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
            entity1.moveTo(entity.getPos().getXd(), entity.getPos().getYd(), entity.getPos().getZd());
            return entity1;
        });
        if(worldEntity == null) {
            throw new EntityLoadException("Entity archetype (" + entity.getId() + ") failed to load: invalid type");
        }
        if(!level.tryAddFreshEntityWithPassengers(worldEntity)) {
            throw new EntityLoadException("Entity failed to add to world: duplicate UUID " + worldEntity.getStringUUID());
        }
        System.out.println("Added entity " + entity.getId() + " at " + entity.getPos()); // debug
    }


    private void clearAllBlockEntities(LevelChunk chunk) {
        Set<BlockPos> pendingBlockEntities = chunk.getBlockEntitiesPos(); // contains pending block entities
        pendingBlockEntities.forEach(chunk::removeBlockEntity);
        chunk.clearAllBlockEntities(); // do rest of the work
    }

    // TODO: review
    @Override
    public void clearChunk(Chunk chunk, boolean clearEntities) { // assumes chunk is already loaded based on retrieval
        final LevelChunk nChunk = (LevelChunk) chunk.asNative();
        final ServerLevel level = nChunk.level;
        final LevelChunk emptyChunk = new LevelChunk(level, nChunk.getPos());

        final ServerChunkCache serverChunkCache = level.getChunkSource();

        // Write empty chunk section to buffer
        final FriendlyByteBuf emptySectionBuf = new FriendlyByteBuf(Unpooled.buffer());
        emptyChunk.getSection(0).write(emptySectionBuf, null, 0);

        // TODO: clear block entities before clearing entities?
        // Remove entities from chunk

        if(clearEntities) {
            // TODO: ReflectionMapping for retrieving native entities
            /*List<Entity> entities = level.getEntityLookup()
                    .getChunk(chunk.getX(), chunk.getZ())
                    .get;*/
            Arrays.stream(level.getChunkEntities(chunk.getX(), chunk.getZ()))
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

        // Resend entire chunk packet
        ClientboundLevelChunkWithLightPacket packet = new ClientboundLevelChunkWithLightPacket(nChunk, serverChunkCache.getLightEngine(), null, null, true);
        nChunk.getChunkHolder().vanillaChunkHolder.broadcast(packet, false);
    }

    @Experimental
    @Override
    public void clearChunks(Chunk e1, Chunk e2, boolean clearEntities) {
        clearChunks(ChunkGetter.SYNC, e1, e2, clearEntities);
    }

    @Experimental
    @Override
    public void clearChunks(ChunkGetter cg, Chunk e1, Chunk e2, boolean clearEntities) {
        clearChunksLogic(e1, e2, pos -> {
            cg.getChunkWCallback(e1.getWorld(), pos.x, pos.z, chunk -> {
                clearChunk(chunk, clearEntities); return chunk;
            });
        });
    }

    private void clearChunksLogic(Chunk e1, Chunk e2, Consumer<ChunkPos> clearChunk) {
        if(e1.getWorld() != e2.getWorld()) {
            throw new IllegalArgumentException("Chunks are not in the same world");
        }

        final LevelChunk nChunk1 = (LevelChunk) e1.asNative();
        final LevelChunk nChunk2 = (LevelChunk) e2.asNative();

        final ServerLevel level = (ServerLevel) e1.getNativeWorld();
        //final ServerChunkCache serverChunkCache = level.getChunkSource();

        Bukkit.broadcast(Component.text("Clearing chunk contents..."));
        ChunkPos.rangeClosed(nChunk1.getPos(), nChunk2.getPos()).forEach(clearChunk);

        //Bukkit.broadcast(Component.text("Relighting..."));
        //serverChunkCache.getLightEngine().relight(getNeighboringChunks(e1, e2), a -> {}, b -> {});

        Bukkit.broadcast(Component.text(String.format("Cleared %d chunk(s)",
                ChunkPos.rangeClosed(nChunk1.getPos(), nChunk2.getPos()).count())));
    }

    @NeedsTesting
    @Override
    public int getDataVersion() {
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
