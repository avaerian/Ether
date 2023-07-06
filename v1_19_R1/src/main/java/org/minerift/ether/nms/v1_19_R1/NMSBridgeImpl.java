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
import net.minecraft.world.level.ChunkPos;
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
import org.bukkit.craftbukkit.v1_19_R1.block.data.CraftBlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.minerift.ether.nms.NMSBridge;
import org.minerift.ether.util.SortedList;
import org.minerift.ether.util.math.Vec3i;
import org.minerift.ether.world.QueuedBlock;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class NMSBridgeImpl implements NMSBridge {

    public static final Function<BlockData, BlockState> BLOCK_DATA_TO_STATE_FUNC = (data) -> ((CraftBlockData) data).getState();

    private void fastClearSingleChunk(ChunkPos pos, World world, boolean clearEntities) {
        fastClearSingleChunk(world.getChunkAt(pos.x, pos.z), clearEntities);
    }

    // Clears a chunk of all blocks/entities
    // Does not perform lighting updates
    private void fastClearSingleChunk(Chunk chunk, boolean clearEntities) {

        if(!chunk.isLoaded()) {
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
                    .filter(entity -> entity.getType() != EntityType.PLAYER)
                    .forEach(Entity::remove);
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

    /*
    // originOffset represents the offset from the origin
    @Deprecated
    private void fastSetBlockss(Set<QueuedBlock> blocks, Location location, Vec3i originOffset) {

        final World world = location.getWorld();

        // Get affected sections and partition blocks
        Set<LevelChunk> chunksChanged = new HashSet<>();
        Map<LevelChunkSection, ChunkSectionChanges> allChanges = new HashMap<>();
        for(QueuedBlock block : blocks) {

            // First, transform position
            block.getPos().add(location.getBlockX(), location.getBlockY(), location.getBlockZ());

            LevelChunk chunk = ((CraftChunk) block.getChunk(world)).getHandle();
            LevelChunkSection section = chunk.getSection(chunk.getSectionIndex(block.getY()));

            SectionPos pos = SectionPos.of(block.getX() >> 4, block.getY() >> 4, block.getZ() >> 4);
            ChunkSectionChanges sectionChanges = allChanges.computeIfAbsent(section, (ignore) -> new ChunkSectionChanges(pos, chunk));

            sectionChanges.blocks.add(block);
            chunksChanged.add(chunk);
        }

        // Apply changes to sections and broadcast
        for(Map.Entry<LevelChunkSection, ChunkSectionChanges> entry : allChanges.entrySet()) {
            final LevelChunkSection section = entry.getKey();
            final ChunkSectionChanges sectionChanges = entry.getValue();

            // Apply blocks and compute changes for packet
            applyBlocksToSection(sectionChanges.parentChunk, section, sectionChanges.blocks, null);
            sectionChanges.computePacketData();

            // Broadcast section update packet
            ClientboundSectionBlocksUpdatePacket packet = new ClientboundSectionBlocksUpdatePacket(sectionChanges.sectionPos, sectionChanges.positions, sectionChanges.states, false);
            ChunkHolder chunkHolder = sectionChanges.parentChunk.getChunkHolder().vanillaChunkHolder;
            chunkHolder.broadcast(packet, false);
        }

        chunksChanged.forEach(chunk -> chunk.setUnsaved(true));
    }*/

    private void fastSetBlocksLogic(Set<QueuedBlock> blocks, Location location, Vec3i originOffset, GetChunkFunction getChunkFunc) {

        // First, transform position
        // TODO: delegate this to the schematic API
        blocks.forEach(block -> block.getPos().add(location.getBlockX(), location.getBlockY(), location.getBlockZ()));

        final BlockPartition partition = new BlockPartition(blocks, location.getWorld(), getChunkFunc);

        // Perform actions on chunks
        partition.forEachChunk((chunk, chunkChanges) -> {

            // Apply changes to sections
            for(Map.Entry<LevelChunkSection, ChunkSectionChanges> entry : chunkChanges.entrySet()) {

                final LevelChunkSection section = entry.getKey();
                final ChunkSectionChanges sectionChanges = entry.getValue();

                applyBlocksToSection(chunk, section, sectionChanges.blocks, partition);
                sectionChanges.computePacketData();

                // Broadcast section update packet
                ClientboundSectionBlocksUpdatePacket packet = new ClientboundSectionBlocksUpdatePacket(sectionChanges.sectionPos, sectionChanges.positions, sectionChanges.states, false);
                ChunkHolder chunkHolder = sectionChanges.parentChunk.getChunkHolder().vanillaChunkHolder;
                chunkHolder.broadcast(packet, false);
            }

            chunk.setUnsaved(true);
        });

    }

    @Override
    public void fastSetBlocks(Set<QueuedBlock> blocks, Location location, Vec3i originOffset) {
        fastSetBlocksLogic(blocks, location, originOffset, GetChunkFunction.SYNC);
    }

    @Override
    public void fastSetBlocksAsync(Set<QueuedBlock> blocks, Location location, Vec3i originOffset) {
        fastSetBlocksLogic(blocks, location, originOffset, GetChunkFunction.ASYNC);
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

        private Map<LevelChunk, Map<LevelChunkSection, ChunkSectionChanges>> partition;
        private World world;
        private final GetChunkFunction chunkGetter;

        private ChunkPos bottomLeft, topRight;

        public BlockPartition(Set<QueuedBlock> blocks, World world, GetChunkFunction chunkGetter) {
            Preconditions.checkArgument(blocks != null && !blocks.isEmpty(), "Queued blocks cannot be empty!");

            // Filter out-of-bound blocks
            blocks = blocks.stream()
                    .filter(block -> block.getY() >= world.getMinHeight() && block.getY() < world.getMaxHeight())
                    .collect(Collectors.toSet());

            this.partition = new HashMap<>();
            this.world = world;
            this.chunkGetter = chunkGetter;
            addAll(blocks);
            findRegionBounds();

        }

        private void findRegionBounds() {

            // Handle for single chunk
            if(getChunks().size() == 1) {
                ChunkPos pos = getChunks().iterator().next().getPos();
                this.bottomLeft = new ChunkPos(pos.x - 1, pos.z - 1);
                this.topRight = new ChunkPos(pos.x + 1, pos.x + 1);
                return;
            }

            // Sort chunks by coordinates
            List<ChunkPos> sortedX = getChunks().stream()
                    .map(ChunkAccess::getPos)
                    .sorted(Comparator.comparing(pos -> pos.x))
                    .toList();

            List<ChunkPos> sortedZ = new ArrayList<>(sortedX);
            sortedZ.sort(Comparator.comparing(pos -> pos.z));

            // Get region bounds
            final int SMALLEST = 0;
            final int LARGEST = sortedX.size() - 1;

            int left = sortedX.get(SMALLEST).x;
            int bottom = sortedZ.get(SMALLEST).z;
            int right = sortedX.get(LARGEST).x;
            int top = sortedZ.get(LARGEST).z;

            // Read values
            this.bottomLeft = new ChunkPos(left - 1, bottom - 1);
            this.topRight = new ChunkPos( right + 1, top + 1);
        }

        public Map<LevelChunkSection, ChunkSectionChanges> getSectionChangesForChunk(LevelChunk chunk) {
            return partition.get(chunk);
        }

        public Set<LevelChunk> getChunks() {
            return partition.keySet();
        }

        public ChunkPos getBottomLeftLightingBound() {
            return bottomLeft;
        }

        public ChunkPos getTopRightLightingBound() {
            return topRight;
        }

        public Set<ChunkPos> getChunksForRelighting() {
            return ChunkPos.rangeClosed(bottomLeft, topRight).collect(Collectors.toSet());
        }

        public void add(QueuedBlock block) {
            chunkGetter.accept(world, block.getPos(), (chunk) -> partitionSingleBlock(block, chunk));
        }

        public void addAll(Collection<QueuedBlock> blocks) {
            for(QueuedBlock block : blocks) {
                add(block);
            }
        }

        private void partitionSingleBlock(QueuedBlock block, Chunk bukkitChunk) {

            final LevelChunk chunk = ((CraftChunk) bukkitChunk).getHandle();
            final LevelChunkSection section = chunk.getSection(chunk.getSectionIndex(block.getY()));
            final SectionPos pos = SectionPos.of(block.getX() >> 4, block.getY() >> 4, block.getZ() >> 4);

            Map<LevelChunkSection, ChunkSectionChanges> chunkChanges = partition.computeIfAbsent(chunk, (ignore) -> new HashMap<>());
            ChunkSectionChanges sectionChanges = chunkChanges.computeIfAbsent(section, (ignore) -> new ChunkSectionChanges(pos, chunk));
            sectionChanges.blocks.add(block);
        }

        public void forEachChunk(BiConsumer<LevelChunk, Map<LevelChunkSection, ChunkSectionChanges>> callback) {
            partition.forEach(callback);
        }
    }

    private void applyBlocksToSection(LevelChunk chunk, LevelChunkSection section, Set<QueuedBlock> blocks, BlockPartition partition) {
        section.acquire();
        try {
            final boolean hasOnlyAirBefore = section.hasOnlyAir();

            // Perform updates
            for(QueuedBlock block : blocks) {

                // Set block
                final int x = SectionPos.sectionRelative(block.getX());
                final int y = SectionPos.sectionRelative(block.getY());
                final int z = SectionPos.sectionRelative(block.getZ());

                BlockState state = block.getState(BLOCK_DATA_TO_STATE_FUNC);
                section.setBlockState(x, y, z, state, false);

                // Update heightmaps
                chunk.heightmaps.get(Heightmap.Types.MOTION_BLOCKING).update(x, block.getY(), z, state);
                chunk.heightmaps.get(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES).update(x, block.getY(), z, state);
                chunk.heightmaps.get(Heightmap.Types.OCEAN_FLOOR).update(x, block.getY(), z, state);
                chunk.heightmaps.get(Heightmap.Types.WORLD_SURFACE).update(x, block.getY(), z, state);
            }

            // Queue light updates
            final boolean hasOnlyAirNow = section.hasOnlyAir();
            if(hasOnlyAirBefore != hasOnlyAirNow) {
                chunk.level.getChunkSource().getLightEngine().relight(partition.getChunksForRelighting(), a -> {}, b -> {});
            }
        } finally {
            section.release();
        }
    }

    private static class ChunkSectionChanges {

        public final Set<QueuedBlock> blocks; // block states to be applied in section
        public final SectionPos sectionPos;
        public final LevelChunk parentChunk;

        // Packet data
        public ShortSet positions;
        public BlockState[] states;

        public ChunkSectionChanges(SectionPos sectionPos, LevelChunk chunk) {
            this.blocks = new HashSet<>();
            this.sectionPos = sectionPos;
            this.parentChunk = chunk;
            this.positions = ShortSets.emptySet();
            this.states = new BlockState[0];
        }

        // Computes data about section changes for section update packet
        public void computePacketData() {
            short[] positions = new short[blocks.size()];
            BlockState[] states = new BlockState[blocks.size()];

            int index = 0;
            BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
            for(Iterator<QueuedBlock> it = blocks.iterator(); it.hasNext(); index++) {
                QueuedBlock block = it.next();
                mutableBlockPos.set(block.getX(), block.getY(), block.getZ());

                positions[index] = SectionPos.sectionRelativePos(mutableBlockPos);
                states[index] = block.getState(BLOCK_DATA_TO_STATE_FUNC);
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

    // Get the neighboring chunks for a single chunk
    private Set<ChunkPos> getNeighboringChunks(Chunk chunk) {
        return getNeighboringChunks(chunk, chunk);
    }

    // Get the neighboring chunks between two endpoints (chunks)
    private Set<ChunkPos> getNeighboringChunks(Chunk e1, Chunk e2) {

        if(e1.getWorld() != e2.getWorld()) {
            throw new IllegalArgumentException("Chunks are not in the same world!");
        }

        int capacity = (e2.getZ() - e1.getZ() + 3) * (e2.getX() - e1.getX() + 3);
        Set<ChunkPos> chunks = new HashSet<>(capacity);
        for(int x = e1.getX() - 1; x <= e2.getX() + 1; x++) {
            for(int z = e1.getZ() - 1; z <= e2.getZ() + 1; z++) {
                chunks.add(new ChunkPos(x, z));
                //System.out.println(String.format("%d, %d", x, z)); // debug
            }
        }

        return chunks;
    }
}
