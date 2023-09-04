package org.minerift.ether.nms.v1_19_R1;

import com.google.common.base.Preconditions;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.shorts.ShortArraySet;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import it.unimi.dsi.fastutil.shorts.ShortSets;
import net.kyori.adventure.text.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_19_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R1.block.data.CraftBlockData;
import org.minerift.ether.EtherPlugin;
import org.minerift.ether.nms.NMSBridge;
import org.minerift.ether.util.math.Vec3i;
import org.minerift.ether.util.nbt.tags.*;
import org.minerift.ether.work.Operation;
import org.minerift.ether.work.WorkQueue;
import org.minerift.ether.world.BlockArchetype;
import org.minerift.ether.world.BlockEntityArchetype;
import org.minerift.ether.world.EntityArchetype;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class NMSBridgeImpl implements NMSBridge {

    private void fastClearSingleChunk(ChunkPos pos, World world, boolean clearEntities) {
        fastClearSingleChunk(world.getChunkAt(pos.x, pos.z), clearEntities);
    }

    // Clears a chunk of all blocks/entities
    // Does not perform lighting updates
    private void fastClearSingleChunk(Chunk chunk, boolean clearEntities) {

        // Load chunk if not already loaded
        boolean chunkInitiallyLoaded = chunk.isLoaded();
        if(!chunkInitiallyLoaded) {
            chunk.load();
        }

        final LevelChunk nmsChunk = ((CraftChunk) chunk).getHandle();
        final LevelChunk emptyChunk = new LevelChunk(nmsChunk.getLevel(), nmsChunk.getPos());

        final ServerLevel serverLevel = nmsChunk.level;
        final ServerChunkCache serverChunkCache = serverLevel.getChunkSource();

        // Write empty chunk section to buffer
        final FriendlyByteBuf emptySectionBuf = new FriendlyByteBuf(Unpooled.buffer());
        emptyChunk.getSection(0).write(emptySectionBuf);

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

        // Clean up
        if(!chunkInitiallyLoaded) { // if the chunk was not initially loaded, we had to load it
            chunk.unload(true);
        }
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

    private void fastSetBlocksLogic(List<BlockArchetype> blocks, World world, GetChunkFunction getChunkFunc) {

        final BlockPartition partition = new BlockPartition(blocks, world, getChunkFunc);

        // Perform actions on chunks
        partition.forEachChunk((chunk, chunkChanges) -> {

            // Apply changes to sections
            for(Map.Entry<LevelChunkSection, ChunkSectionChanges> entry : chunkChanges.entrySet()) {

                final LevelChunkSection section = entry.getKey();
                final ChunkSectionChanges sectionChanges = entry.getValue();

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
        fastSetBlocksLogic(blocks, world, GetChunkFunction.SYNC);
    }

    @Override
    public void fastSetBlocksAsync(List<BlockArchetype> blocks, World world) {
        fastSetBlocksLogic(blocks, world, GetChunkFunction.ASYNC);
    }

    @Override
    public void fastSetBlocksAsyncLazy(List<BlockArchetype> blocks, World world) {

        final BlockPartition partition = new BlockPartition(blocks, world, GetChunkFunction.ASYNC);
        final WorkQueue workQueue = EtherPlugin.getInstance().getWorkQueue();
        final Operation operation = new Operation();

        // Perform actions on chunks
        partition.forEachChunk((chunk, chunkChanges) -> {

            // Apply changes to sections
            for(Map.Entry<LevelChunkSection, ChunkSectionChanges> entry : chunkChanges.entrySet()) {

                final LevelChunkSection section = entry.getKey();
                final ChunkSectionChanges sectionChanges = entry.getValue();

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

        // Queue light updates after operation
        operation.whenComplete((success, failReason) -> {
            if(failReason == Operation.FailReason.QUEUE_SHUTDOWN) {
                // TODO: replace with proper logger
                System.out.println("Queue shutdown, so lazy block-setting operation failed.");
                System.out.println(String.format("Failed to execute %d tasks for operation.", operation.getRemainingTaskCount()));
            }

            if(success) {
                final ServerLevel level = ((CraftWorld) world).getHandle();
                level.getChunkSource().getLightEngine().relight(partition.getChunksForRelighting(), a -> {}, b -> {});

                /*
                // Save to disk so new changes can be flushed from memory
                if(partition.getTotalBlockChanges() > (100 * 100 * 100)) {
                    level.save(null, true, level.noSave(), false);
                }*/
            }
        });

        // Queue work for execution
        workQueue.enqueue(operation);
    }

    private interface GetChunkFunction {
        void accept(World world, Vec3i pos, Consumer<Chunk> cb);

        GetChunkFunction SYNC = (world, pos, chunkCallback) -> {
            chunkCallback.accept(world.getChunkAt(pos.getX() >> 4, pos.getZ() >> 4));
        };

        GetChunkFunction ASYNC = (world, pos, chunkCallback) -> {
            world.getChunkAtAsync(pos.getX() >> 4, pos.getZ() >> 4, chunkCallback);
        };
    }

    private static class BlockPartition {

        private final Map<LevelChunk, Map<LevelChunkSection, ChunkSectionChanges>> partition;
        private final World world;
        private final GetChunkFunction chunkGetter;
        private int totalBlockCount;

        private ChunkPos bottomLeft, topRight;

        public BlockPartition(List<BlockArchetype> blocks, World world, GetChunkFunction chunkGetter) {
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
            findRegionBounds();
        }

        private void findRegionBounds() {

            final Set<LevelChunk> chunks = getChunks();

            // TODO: handle this better for cases of no blocks
            if(partition.isEmpty()) {
                this.bottomLeft = null;
                this.topRight = null;
                return;
            }

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

        public Map<LevelChunkSection, ChunkSectionChanges> getChunkChanges(LevelChunk chunk) {
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
            return getNeighboringChunks(bottomLeft, topRight);
        }

        public void add(BlockArchetype block) {
            chunkGetter.accept(world, block.getPos(), (chunk) -> partitionSingleBlock(block, chunk));
            totalBlockCount++;
        }

        public void addAll(Collection<BlockArchetype> blocks) {
            blocks.forEach(this::add);
        }

        public int getTotalBlockChanges() {
            return totalBlockCount;
        }

        // Simple cache for partition method
        private LevelChunkSection lastSection = null;
        private ChunkSectionChanges lastSectionChanges = null;

        private void partitionSingleBlock(BlockArchetype block, Chunk bukkitChunk) {
            final LevelChunk chunk = ((CraftChunk) bukkitChunk).getHandle();
            final LevelChunkSection section = chunk.getSection(chunk.getSectionIndex(block.getY()));
            final SectionPos pos = SectionPos.of(block.getX() >> 4, block.getY() >> 4, block.getZ() >> 4);

            // Check if cached and update if needed
            ChunkSectionChanges sectionChanges;
            if(section != lastSection) {
                // Prepare partition
                Map<LevelChunkSection, ChunkSectionChanges> chunkChanges = partition.computeIfAbsent(chunk, (ignore) -> new HashMap<>());
                sectionChanges = chunkChanges.computeIfAbsent(section, (ignore) -> new ChunkSectionChanges(pos, totalBlockCount));
            } else {
                sectionChanges = lastSectionChanges;
            }

            // Partition block
            sectionChanges.blocks.add(block);

            // Update cache
            this.lastSection = section;
            this.lastSectionChanges = sectionChanges;
        }

        public void forEachChunk(BiConsumer<LevelChunk, Map<LevelChunkSection, ChunkSectionChanges>> callback) {
            partition.forEach(callback);
        }
    }

    private void applyBlocksToSection(LevelChunk chunk, LevelChunkSection section, List<BlockArchetype> blocks) {
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
                BlockState state = toNativeBlockState(block, Blocks.AIR.defaultBlockState());
                BlockState oldState = section.setBlockState(x, y, z, state, false);

                // Remove old block entity, if needed
                if(oldState.hasBlockEntity()) {
                    chunk.removeBlockEntity(blockPos);
                }

                // Create new block entity, if needed
                if(state.hasBlockEntity()) {
                    BlockEntity blockEntity = ((EntityBlock) state.getBlock()).newBlockEntity(blockPos, state);
                    if(blockEntity != null) {
                        chunk.addAndRegisterBlockEntity(blockEntity);

                        // Load NBT data
                        if(block instanceof BlockEntityArchetype blockEntityArchetype) {
                            net.minecraft.nbt.CompoundTag nbt = (net.minecraft.nbt.CompoundTag) toNativeTag(blockEntityArchetype.getNBTData());
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

    private static class ChunkSectionChanges {

        public static final int BLOCKS_PER_SECTION = 16 * 16 * 16; // 4096

        public final List<BlockArchetype> blocks; // block states to be applied in section
        public final SectionPos sectionPos;

        // Packet data
        public ShortSet positions;
        public BlockState[] states;

        public ChunkSectionChanges(SectionPos sectionPos, int totalBlockCount) {
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
            for(Iterator<BlockArchetype> it = blocks.iterator(); it.hasNext(); index++) {
                BlockArchetype block = it.next();
                mutableBlockPos.set(block.getX(), block.getY(), block.getZ());

                positions[index] = SectionPos.sectionRelativePos(mutableBlockPos);
                states[index] = toNativeBlockState(block);
            }

            this.positions = new ShortArraySet(positions);
            this.states = states;
        }

    }

    private void fastClearChunksLogic(Chunk e1, Chunk e2, Consumer<ChunkPos> clearChunk) {

        if(e1.getWorld() != e2.getWorld()) {
            throw new IllegalArgumentException("Chunks are not in the same world!");
        }

        final LevelChunk nmsChunk1 = ((CraftChunk) e1).getHandle();
        final LevelChunk nmsChunk2 = ((CraftChunk) e2).getHandle();

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
            final net.minecraft.nbt.CompoundTag nbt = (net.minecraft.nbt.CompoundTag) toNativeTag(entityArchetype.getNbtData());
            final Entity entity = type.create(level);
            if(entity != null) {
                entity.load(nbt);
            }
        });

        // TODO
        //return success.get();

        return null;
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

        final LevelChunk chunk = ((CraftChunk)location.getChunk()).getHandle();
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

        final ChunkPos p1 = ((CraftChunk)e1).getHandle().getPos();
        final ChunkPos p2 = ((CraftChunk)e2).getHandle().getPos();

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

    private static LevelChunkSection getChunkSectionAt(Location loc) {
        return getChunkSectionAt(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), loc.getWorld());
    }

    private static LevelChunkSection getChunkSectionAt(int x, int y, int z, World world) {
        final Chunk bukkitChunk = world.getChunkAt(x >> 4, z >> 4);
        final LevelChunk chunk = ((CraftChunk)bukkitChunk).getHandle();
        return chunk.getSection(chunk.getSectionIndex(y));
    }


    // Get the neighboring chunks for a single chunk
    private static Set<ChunkPos> getNeighboringChunks(Chunk chunk) {
        return getNeighboringChunks(chunk, chunk);
    }

    // Get the neighboring chunks between two endpoints (chunks)
    private static Set<ChunkPos> getNeighboringChunks(Chunk e1, Chunk e2) {

        if(e1.getWorld() != e2.getWorld()) {
            throw new IllegalArgumentException("Chunks are not in the same world!");
        }

        // Get original bounds
        ChunkPos p1 = ((CraftChunk)e1).getHandle().getPos();
        ChunkPos p2 = ((CraftChunk)e2).getHandle().getPos();

        return getNeighboringChunks(p1, p2);
    }

    private static Set<ChunkPos> getNeighboringChunks(ChunkPos e1, ChunkPos e2) {
        // Adjust for neighbors
        e1 = new ChunkPos(e1.x - 1, e1.z - 1);
        e2 = new ChunkPos(e2.x + 1, e2.z + 1);

        // Get chunks with neighbors
        return ChunkPos.rangeClosed(e1, e2).collect(Collectors.toSet());
    }



    // TODO: refactor into adapter class for NMS/Ether conversions
    private static net.minecraft.nbt.Tag toNativeTag(Tag tag) {
        if(tag == null) {
            return null;
        }

        return switch (tag.getTagType()) {

            case END_TAG -> net.minecraft.nbt.EndTag.INSTANCE;

            // Primitives
            case BYTE_TAG   -> net.minecraft.nbt.ByteTag.valueOf(((ByteTag)tag).getValue());
            case SHORT_TAG  -> net.minecraft.nbt.ShortTag.valueOf(((ShortTag)tag).getValue());
            case INT_TAG    -> net.minecraft.nbt.IntTag.valueOf(((IntTag)tag).getValue());
            case LONG_TAG   -> net.minecraft.nbt.LongTag.valueOf(((LongTag)tag).getValue());
            case FLOAT_TAG  -> net.minecraft.nbt.FloatTag.valueOf(((FloatTag)tag).getValue());
            case DOUBLE_TAG -> net.minecraft.nbt.DoubleTag.valueOf(((DoubleTag)tag).getValue());
            case STRING_TAG -> net.minecraft.nbt.StringTag.valueOf(((StringTag)tag).getValue());

            // Arrays
            case BYTE_ARRAY_TAG -> new net.minecraft.nbt.ByteArrayTag(((ByteArrayTag)tag).getValue());
            case INT_ARRAY_TAG  -> new net.minecraft.nbt.IntArrayTag(((IntArrayTag)tag).getValue());
            case LONG_ARRAY_TAG -> new net.minecraft.nbt.LongArrayTag(((LongArrayTag)tag).getValue());

            // Collections
            case LIST_TAG -> {
                final ListTag listTag = ((ListTag) tag);
                final net.minecraft.nbt.ListTag nativeTag = new net.minecraft.nbt.ListTag();
                listTag.getValue().forEach(tagInList -> nativeTag.add(toNativeTag(tagInList)));
                yield nativeTag;
            }

            case COMPOUND_TAG -> {
                final CompoundTag compoundTag = (CompoundTag) tag;
                final net.minecraft.nbt.CompoundTag nativeTag = new net.minecraft.nbt.CompoundTag();
                for(Map.Entry<String, Tag> entry : compoundTag.getValue().entrySet()) {
                    nativeTag.put(entry.getKey(), toNativeTag(entry.getValue()));
                }
                yield nativeTag;
            }
        };
    }

    private static BlockState toNativeBlockState(BlockArchetype block) {
        return toNativeBlockState(block.getId());
    }

    private static BlockState toNativeBlockState(BlockArchetype block, BlockState fallback) {
        return toNativeBlockState(block.getId(), fallback);
    }

    private static BlockState toNativeBlockState(BlockData data) {
        Preconditions.checkNotNull(data, "BlockData cannot be null!");
        return ((CraftBlockData)data).getState();
    }

    // Returns null if block state can't be read
    private static BlockState toNativeBlockState(String id) {
        return toNativeBlockState(id, null);
    }

    // Returns the fallback if the block state can't be read
    private static BlockState toNativeBlockState(String id, BlockState fallback) {
        try {
            return toNativeBlockState(Bukkit.createBlockData(id));
        } catch (IllegalArgumentException ex) {
            return fallback;
        }
    }
}
