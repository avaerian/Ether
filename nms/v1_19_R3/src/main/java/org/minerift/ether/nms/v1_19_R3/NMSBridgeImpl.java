package org.minerift.ether.nms.v1_19_R3;

import com.google.common.base.Preconditions;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.shorts.ShortArraySet;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import it.unimi.dsi.fastutil.shorts.ShortSets;
import net.kyori.adventure.text.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_19_R3.CraftWorld;
import org.minerift.ether.Ether;
import org.minerift.ether.nms.NMSBridge;
import org.minerift.ether.util.reflect.Reflect;
import org.minerift.ether.util.reflect.ReflectedObject;
import org.minerift.ether.work.TaskBatch;
import org.minerift.ether.world.BlockArchetype;
import org.minerift.ether.world.BlockEntityArchetype;
import org.minerift.ether.world.EntityArchetype;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.minerift.ether.nms.v1_19_R3.NativeTypeConversions.*;

public class NMSBridgeImpl implements NMSBridge {

    @Override
    public void bootstrap() {
        ReflectionMappings.class.getClass(); // load class and fields for reflection
        System.out.println("frozen registry obfuscated field: " + ReflectionMappings.FROZEN_REGISTRY_FIELD);
        ReflectedObject<Registry<Biome>> refBiomeRegistry = Reflect.of(MinecraftServer.getServer().registryAccess().registryOrThrow(Registries.BIOME));
        System.out.println("Biome Registry Frozen? " + (boolean) refBiomeRegistry.readField(ReflectionMappings.FROZEN_REGISTRY_FIELD));
    }

    private void fastClearSingleChunk(ChunkPos pos, World world, boolean clearEntities) {
        fastClearSingleChunk(world.getChunkAt(pos.x, pos.z), clearEntities);
    }

    // Clears a chunk of all blocks/entities
    // Does not perform lighting updates
    private void fastClearSingleChunk(Chunk chunk, boolean clearEntities) {

        final LevelChunk nmsChunk = toNativeChunk(chunk);
        final ServerLevel level = nmsChunk.level;
        final LevelChunk emptyChunk = new LevelChunk(level, nmsChunk.getPos());

        final ServerChunkCache serverChunkCache = level.getChunkSource();

        // Write empty chunk section to buffer
        final FriendlyByteBuf emptySectionBuf = new FriendlyByteBuf(Unpooled.buffer());
        emptyChunk.getSection(0).write(emptySectionBuf);

        // TODO: clear block entities before clearing entities?
        // Remove entities from chunk
        if(clearEntities) {
            Arrays.stream(chunk.getEntities())
                    .filter(entity -> entity.getType() != org.bukkit.entity.EntityType.PLAYER)
                    .forEach(org.bukkit.entity.Entity::remove);
        }

        // Update chunk and sections
        clearAllBlockEntities(nmsChunk);
        for(LevelChunkSection section : nmsChunk.getSections()) {
            section.read(emptySectionBuf);
            section.recalcBlockCounts();
            emptySectionBuf.resetReaderIndex();
        }

        // Update heightmaps for chunk
        for(Heightmap.Types type : ChunkStatus.FULL.heightmapsAfter()) {
            nmsChunk.setHeightmap(type, emptyChunk.heightmaps.get(type).getRawData());
        }

        nmsChunk.setBlockEmptinessMap(emptyChunk.getBlockEmptinessMap());
        nmsChunk.setSkyEmptinessMap(emptyChunk.getSkyEmptinessMap());
        nmsChunk.setBlockNibbles(emptyChunk.getBlockNibbles());
        nmsChunk.setSkyNibbles(emptyChunk.getSkyNibbles());

        nmsChunk.setUnsaved(true);

        // Resend entire chunk packet
        ClientboundLevelChunkWithLightPacket packet = new ClientboundLevelChunkWithLightPacket(nmsChunk, serverChunkCache.getLightEngine(), null, null, true, true);
        nmsChunk.getChunkHolder().vanillaChunkHolder.broadcast(packet, false);
    }

    private void clearAllBlockEntities(LevelChunk chunk) {
        Set<BlockPos> pendingBlockEntities = chunk.getBlockEntitiesPos(); // contains pending block entities
        pendingBlockEntities.forEach(chunk::removeBlockEntity);
        chunk.clearAllBlockEntities(); // do rest of the work
    }

    @Override
    public void fastClearChunk(Chunk chunk, boolean clearEntities) {
        fastClearChunks(chunk, chunk, clearEntities);
    }

    @Override
    public void fastClearChunks(Chunk e1, Chunk e2, boolean clearEntities) {
        fastClearChunksLogic(e1, e2, pos -> {
            fastClearSingleChunk(pos, e1.getWorld(), clearEntities);
        });
    }

    @Override
    public void fastClearChunksAsync(Chunk e1, Chunk e2, boolean clearEntities) {
        fastClearChunksLogic(e1, e2, pos -> {
            e1.getWorld().getChunkAtAsync(pos.x, pos.z, true, chunk -> fastClearSingleChunk(chunk, clearEntities));
        });
    }

    private void fastSetBlocksLogic(List<BlockArchetype> blocks, World world, DeprecatedGetChunkFunction getChunkFunc) {

        final DeprecatedBlockPartition partition = new DeprecatedBlockPartition(blocks, world, getChunkFunc);

        // Perform actions on chunks
        partition.forEachChunk((chunk, chunkChanges) -> {

            // Apply changes to sections
            for(Map.Entry<LevelChunkSection, DeprecatedChunkSectionChanges> entry : chunkChanges.entrySet()) {

                final LevelChunkSection section = entry.getKey();
                final DeprecatedChunkSectionChanges sectionChanges = entry.getValue();

                applyBlocksToSection(chunk, section, sectionChanges.blocks);
                sectionChanges.computePacketData();

                // Broadcast section update packet
                ClientboundSectionBlocksUpdatePacket packet = new ClientboundSectionBlocksUpdatePacket(sectionChanges.sectionPos, sectionChanges.positions, sectionChanges.states, false);
                ChunkHolder chunkHolder = chunk.getChunkHolder().vanillaChunkHolder;
                chunkHolder.broadcast(packet, false);
            }

            chunk.setUnsaved(true);
        });

        // Queue light updates
        final ServerLevel level = ((CraftWorld) world).getHandle();
        level.getChunkSource().getLightEngine().relight(partition.getChunksForRelighting(), a -> {}, b -> {});
    }

    @Override
    public void fastSetBlocks(List<BlockArchetype> blocks, World world) {
        fastSetBlocksLogic(blocks, world, DeprecatedGetChunkFunction.SYNC);
    }

    @Override
    public void fastSetBlocksAsync(List<BlockArchetype> blocks, World world) {
        fastSetBlocksLogic(blocks, world, DeprecatedGetChunkFunction.ASYNC);
    }

    @Override
    public void fastSetBlocksAsyncLazy(List<BlockArchetype> blocks, World world) {

        final DeprecatedBlockPartition partition = new DeprecatedBlockPartition(blocks, world, DeprecatedGetChunkFunction.ASYNC);
        //final WorkQueue workQueue = EtherPlugin.getInstance().getWorkQueue();
        final Logger logger = Ether.getLogger();
        final TaskBatch operation = new TaskBatch();

        System.out.println("Partition Chunks = " + partition.getChunks());

        // Perform actions on chunks
        partition.forEachChunk((chunk, chunkChanges) -> {

            // Apply changes to sections
            for(Map.Entry<LevelChunkSection, DeprecatedChunkSectionChanges> entry : chunkChanges.entrySet()) {

                final LevelChunkSection section = entry.getKey();
                final DeprecatedChunkSectionChanges sectionChanges = entry.getValue();

                operation.addTask(() -> {

                    // Apply blocks
                    applyBlocksToSection(chunk, section, sectionChanges.blocks);
                    sectionChanges.computePacketData();

                    // Broadcast section update packet
                    ClientboundSectionBlocksUpdatePacket packet = new ClientboundSectionBlocksUpdatePacket(sectionChanges.sectionPos, sectionChanges.positions, sectionChanges.states, false);
                    ChunkHolder chunkHolder = chunk.getChunkHolder().vanillaChunkHolder;
                    chunkHolder.broadcast(packet, false);

                    chunk.setUnsaved(true);

                    // Completed successfully
                    return true;
                });
            }
        });

        operation.whenComplete((status) -> {

            switch(status) {
                // Queue light updates after operation completes
                case OP_COMPLETE -> {
                    final ServerLevel level = ((CraftWorld) world).getHandle();
                    level.getChunkSource().getLightEngine().relight(partition.getChunksForRelighting(), a -> {}, b -> {});

                    /*
                    // Save to disk so new changes can be flushed from memory?
                    if(partition.getTotalBlockChanges() > (100 * 100 * 100)) {
                        level.save(null, true, level.noSave(), false);
                    }*/
                }

                case QUEUE_SHUTDOWN -> {
                    logger.log(Level.WARNING, "Queue shutdown, so lazy block-setting operation failed.");
                    logger.log(Level.WARNING, String.format("Failed to execute %d tasks for operation.", operation.getRemainingTaskCount()));
                }
            }
        });

        // Queue work for execution
        Ether.getWorkQueue().enqueue(operation);
    }

    // Deprecated for removal because block partition should not have to load regions in world to partition block archetypes
    @Deprecated(forRemoval = true)
    private static class DeprecatedBlockPartition {

        private final Map<LevelChunk, Map<LevelChunkSection, DeprecatedChunkSectionChanges>> partition;
        private final World world;
        private final DeprecatedGetChunkFunction chunkGetter;
        private int totalBlockCount;

        private ChunkPos bottomLeft, topRight;

        public DeprecatedBlockPartition(List<BlockArchetype> blocks, World world, DeprecatedGetChunkFunction chunkGetter) {
            Preconditions.checkArgument(blocks != null && !blocks.isEmpty(), "Queued blocks cannot be empty!");

            // Filter out-of-bound blocks
            blocks = blocks.stream()
                    .filter(block -> block.getY() >= world.getMinHeight() && block.getY() < world.getMaxHeight())
                    //.filter(block -> !((CraftWorld) world).getHandle().isOutsideBuildHeight(block.getY()))
                    .toList();

            this.partition = new HashMap<>();
            this.world = world;
            this.chunkGetter = chunkGetter;
            this.totalBlockCount = 0;
            addAll(blocks);

            // TODO: handle this better for cases of no blocks
            this.bottomLeft = null;
            this.topRight = null;
            if(!partition.isEmpty()) {
                findRegionBounds();
            }
        }

        private void findRegionBounds() {

            final Set<LevelChunk> chunks = getChunks();

            // Handle for single chunk
            if(chunks.size() == 1) {
                ChunkPos pos = chunks.iterator().next().getPos();
                this.bottomLeft = new ChunkPos(pos.x, pos.z);
                this.topRight = bottomLeft;
                return;
            }

            // Sort chunks by coordinates
            List<ChunkPos> positions = new ArrayList<>(chunks.stream()
                    .map(ChunkAccess::getPos)
                    .sorted(Comparator.comparing(pos -> pos.x))
                    .toList());

            // Get region bounds
            final int SMALLEST = 0;
            final int LARGEST = positions.size() - 1;

            int left = positions.get(SMALLEST).x;
            int right = positions.get(LARGEST).x;

            positions.sort(Comparator.comparing(pos -> pos.z));

            int bottom = positions.get(SMALLEST).z;
            int top = positions.get(LARGEST).z;

            // Read values
            this.bottomLeft = new ChunkPos(left, bottom);
            this.topRight = new ChunkPos(right, top);
        }

        public Map<LevelChunkSection, DeprecatedChunkSectionChanges> getChunkChanges(LevelChunk chunk) {
            return partition.get(chunk);
        }

        public Set<LevelChunk> getChunks() {
            return partition.keySet();
        }

        public ChunkPos getBottomLeftBound() {
            return bottomLeft;
        }

        public ChunkPos getTopRightBound() {
            return topRight;
        }

        public Set<ChunkPos> getChunksForRelighting() {
            return partition.isEmpty()
                    ? Collections.emptySet()
                    : getNeighboringChunks(bottomLeft, topRight);
        }

        public void add(BlockArchetype block) {
            chunkGetter.accept(world, block.getPos(), (chunk) -> partitionSingleBlock(block, chunk));
            totalBlockCount++;
        }

        public void addAll(Collection<BlockArchetype> blocks) {
            blocks.forEach(this::add);
            System.out.println("Requested block count = " + blocks.size());
            System.out.println("Partition size = " + this.partition.size());
            findRegionBounds();
        }

        public int getTotalBlockChanges() {
            return totalBlockCount; // TODO: calculate from partition instead of storing num?
        }

        // Simple cache for partition method
        private LevelChunkSection lastSection = null;
        private DeprecatedChunkSectionChanges lastSectionChanges = null;

        private void partitionSingleBlock(BlockArchetype block, Chunk bukkitChunk) {
            final LevelChunk chunk = toNativeChunk(bukkitChunk);
            final LevelChunkSection section = chunk.getSection(chunk.getSectionIndex(block.getY()));
            final SectionPos pos = SectionPos.of(block.getX() >> 4, block.getY() >> 4, block.getZ() >> 4);

            // Check if cached and update if needed
            DeprecatedChunkSectionChanges sectionChanges;
            if(section != lastSection) {
                // Prepare partition
                Map<LevelChunkSection, DeprecatedChunkSectionChanges> chunkChanges = partition.computeIfAbsent(chunk, (ignore) -> new HashMap<>());
                sectionChanges = chunkChanges.computeIfAbsent(section, (ignore) -> new DeprecatedChunkSectionChanges(pos, totalBlockCount));
            } else {
                sectionChanges = lastSectionChanges;
            }

            // Partition block
            sectionChanges.blocks.add(block);

            // Update cache
            this.lastSection = section;
            this.lastSectionChanges = sectionChanges;
        }

        public void forEachChunk(BiConsumer<LevelChunk, Map<LevelChunkSection, DeprecatedChunkSectionChanges>> callback) {
            partition.forEach(callback);
        }
    }

    public static void applyBlocksToSection(ChunkAccess chunk, LevelChunkSection section, List<BlockArchetype> blocks) {
        section.acquire();
        try {
            final BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();

            // Perform updates
            for(BlockArchetype block : blocks) {

                // Update position
                blockPos.set(block.getX(), block.getY(), block.getZ());

                final int x = SectionPos.sectionRelative(block.getX());
                final int y = SectionPos.sectionRelative(block.getY());
                final int z = SectionPos.sectionRelative(block.getZ());

                // Set block
                BlockState state = toNative(block, Blocks.AIR.defaultBlockState());
                BlockState oldState = section.setBlockState(x, y, z, state, false);

                // Remove old block entity, if needed
                if(oldState.hasBlockEntity()) {
                    chunk.removeBlockEntity(blockPos);
                }

                // Create new block entity, if needed
                if(state.hasBlockEntity()) {
                    BlockEntity blockEntity = ((EntityBlock) state.getBlock()).newBlockEntity(blockPos, state);
                    if(blockEntity != null) {
                        chunk.setBlockEntity(blockEntity);

                        // Load NBT data
                        if(block instanceof BlockEntityArchetype blockEntityArchetype) {
                            CompoundTag nbt = (CompoundTag) toNative(blockEntityArchetype.getNBTData());
                            blockEntity.load(nbt);
                            blockEntity.setChanged();
                        }
                    }
                }

                // Update heightmaps
                chunk.heightmaps.get(Heightmap.Types.MOTION_BLOCKING).update(x, block.getY(), z, state);
                chunk.heightmaps.get(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES).update(x, block.getY(), z, state);
                chunk.heightmaps.get(Heightmap.Types.OCEAN_FLOOR).update(x, block.getY(), z, state);
                chunk.heightmaps.get(Heightmap.Types.WORLD_SURFACE).update(x, block.getY(), z, state);
            }
        } finally {
            section.release();
        }
    }

    private void fastClearChunksLogic(Chunk e1, Chunk e2, Consumer<ChunkPos> clearChunk) {

        if(e1.getWorld() != e2.getWorld()) {
            throw new IllegalArgumentException("Chunks are not in the same world!");
        }

        final LevelChunk nmsChunk1 = toNativeChunk(e1);
        final LevelChunk nmsChunk2 = toNativeChunk(e2);

        final ServerLevel serverLevel = nmsChunk1.level;
        final ServerChunkCache serverChunkCache = serverLevel.getChunkSource();

        Bukkit.broadcast(Component.text("Clearing chunk contents..."));
        ChunkPos.rangeClosed(nmsChunk1.getPos(), nmsChunk2.getPos()).forEach(clearChunk);

        Bukkit.broadcast(Component.text("Relighting..."));
        serverChunkCache.getLightEngine().relight(getNeighboringChunks(e1, e2), a -> {}, b -> {});

        Bukkit.broadcast(Component.text(String.format("Cleared %d chunk(s)", ChunkPos.rangeClosed(nmsChunk1.getPos(), nmsChunk2.getPos()).count())));
    }


    // This method assumes that the entity archetype position has already been translated (no normalized coordinates)
    @Override
    public org.bukkit.entity.Entity spawnEntity(EntityArchetype entityArchetype, World world) {

        final ServerLevel level = ((CraftWorld)world).getHandle();

        // Attempt to spawn entity
        //EntityType.loadEntityRecursive() // this spawns the entities that can be spawned and logs failed entities
        EntityType.byString(entityArchetype.getType()).ifPresent(type -> {
            final net.minecraft.nbt.CompoundTag nbt = (net.minecraft.nbt.CompoundTag) toNative(entityArchetype.getNbtData());
            final Entity entity = type.create(level);
            if(entity != null) {
                entity.load(nbt);
            }
        });

        // TODO
        //return success.get();

        return null;
    }

    @Override
    public void testNewPartitionPaster(List<BlockArchetype> blocks, World world) {
        BlockPartition partition = new BlockPartition(blocks, world.getMinHeight());
        PartitionPaster.paste(world, partition);
    }

    // Single chunk section
    @Override
    public void testIslandScanIdea(Location location) {

        // Hacky way of trying to store int by reference
        final Map<BlockState, int[]> stateCounts = new HashMap<>();

        final LevelChunkSection section = getChunkSectionAt(location);
        section.getStates().forEachLocation((state, loc) -> {
            int[] count = stateCounts.computeIfAbsent(state, (ignore) -> new int[1]);
            count[0]++;
        });

        stateCounts.forEach((key, value) -> System.out.println(key + " : " + value[0]));
    }

    // Full chunk
    @Override
    public void testIslandScanIdeaFullChunk(Location location) {

        final Map<BlockState, int[]> stateCounts = new HashMap<>();

        final LevelChunk chunk = toNativeChunk(location.getChunk());
        for(LevelChunkSection section : chunk.getSections()) {
            section.getStates().forEachLocation((state, loc) -> {
                int[] count = stateCounts.computeIfAbsent(state, (ignore) -> new int[1]);
                count[0]++;
            });
        }

        stateCounts.forEach((key, value) -> System.out.println(key + " : " + value[0]));
    }

    // Multiple chunks
    // Scanning blocks to calculate island value
    @Override
    public void testIslandScanIdeaMultiChunk(Location location, int diameter) {

        final Map<BlockState, int[]> stateCounts = new HashMap<>();
        //final Object2IntMap<BlockState> stateCounts_ = new Object2IntOpenHashMap<>();
        // TODO: switch to Object2IntMap for performance/better code quality

        final World world = location.getWorld();
        final ServerLevel level = ((CraftWorld)world).getHandle();

        final int centerX = location.getChunk().getX();
        final int centerZ = location.getChunk().getZ();

        int radius = (diameter - 1) / 2;

        final Chunk e1 = world.getChunkAt(centerX - radius, centerZ - radius);
        final Chunk e2 = world.getChunkAt(centerX + radius, centerZ + radius);

        final ChunkPos p1 = toNativeChunkAccess(e1).getPos();
        final ChunkPos p2 = toNativeChunkAccess(e2).getPos();

        ChunkPos.rangeClosed(p1, p2).forEach(pos -> {
            final LevelChunk chunk = level.getChunk(pos.x, pos.z);
            for(LevelChunkSection section : chunk.getSections()) {
                // TODO: include cache for performance boost
                section.getStates().forEachLocation((state, loc) -> {
                    int[] count = stateCounts.computeIfAbsent(state, (ignore) -> new int[1]);
                    count[0]++;
                });
            }
        });

        stateCounts.forEach((key, value) -> System.out.println(key + " : " + value[0]));
    }

    @Override
    public NamespacedKey getBiomeAt(World world, int x, int y, int z) {
        ServerLevel level = ((CraftWorld) world).getHandle();
        Holder<Biome> biomeHolder = level.getBiomeManager().getNoiseBiomeAtPosition(new BlockPos(x, y, z));
        Biome biome = biomeHolder.value();
        //Biome biome = level.getBiome().value();

        System.out.println(biome == null ? "null" : biome.toString());
        return fromNative(biome);
    }

    private static LevelChunkSection getChunkSectionAt(Location loc) {
        return getChunkSectionAt(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), loc.getWorld());
    }

    private static LevelChunkSection getChunkSectionAt(int x, int y, int z, World world) {
        final Chunk bukkitChunk = world.getChunkAt(x >> 4, z >> 4);
        final LevelChunk chunk = toNativeChunk(bukkitChunk);
        return chunk.getSection(chunk.getSectionIndex(y));
    }


    // Get the neighboring chunks for a single chunk
    private static Set<ChunkPos> getNeighboringChunks(Chunk chunk) {
        return getNeighboringChunks(chunk, chunk);
    }

    @Deprecated
    // Get the neighboring chunks between two endpoints (chunks)
    private static Set<ChunkPos> getNeighboringChunks(Chunk e1, Chunk e2) {

        if(e1.getWorld() != e2.getWorld()) {
            throw new IllegalArgumentException("Chunks are not in the same world!");
        }

        // Get original bounds
        ChunkPos p1 = toNativeChunkAccess(e1).getPos();
        ChunkPos p2 = toNativeChunkAccess(e2).getPos();

        return getNeighboringChunks(p1, p2);
    }

    @Deprecated
    public static Set<ChunkPos> getNeighboringChunks(ChunkPos e1, ChunkPos e2) {
        // Adjust for neighbors
        ChunkPos updatedE1 = new ChunkPos(e1.x - 1, e1.z - 1);
        ChunkPos updatedE2 = new ChunkPos(e2.x + 1, e2.z + 1);

        // Get chunks with neighbors
        return ChunkPos.rangeClosed(updatedE1, updatedE2).collect(Collectors.toSet());
    }

    @Deprecated
    public static class DeprecatedChunkSectionChanges {

        public static final int BLOCKS_PER_SECTION = 16 * 16 * 16; // 4096

        public final List<BlockArchetype> blocks; // block states to be applied in section
        public final SectionPos sectionPos;

        // Packet data
        public ShortSet positions;
        public BlockState[] states;

        public DeprecatedChunkSectionChanges(SectionPos sectionPos, int totalBlockCount) {
            this.blocks = new ArrayList<>(totalBlockCount >= BLOCKS_PER_SECTION
                    ? BLOCKS_PER_SECTION
                    : BLOCKS_PER_SECTION / 4);
            this.sectionPos = sectionPos;
            this.positions = ShortSets.emptySet();
            this.states = new BlockState[0];
        }

        // Computes data about section changes for section update packet
        public void computePacketData() {
            short[] positions = new short[blocks.size()];
            BlockState[] states = new BlockState[blocks.size()];

            int index = 0;
            BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
            for (Iterator<BlockArchetype> it = blocks.iterator(); it.hasNext(); index++) {
                BlockArchetype block = it.next();
                mutableBlockPos.set(block.getX(), block.getY(), block.getZ());

                positions[index] = SectionPos.sectionRelativePos(mutableBlockPos);
                states[index] = toNative(block);
            }

            this.positions = new ShortArraySet(positions);
            this.states = states;
        }

    }
}
