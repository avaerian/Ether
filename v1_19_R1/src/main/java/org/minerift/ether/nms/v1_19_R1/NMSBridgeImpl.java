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
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_19_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_19_R1.block.CraftBlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.minerift.ether.nms.NMSBridge;

import java.util.*;
import java.util.function.Consumer;

public class NMSBridgeImpl implements NMSBridge {

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

    @Override
    public void fastSetBlock(Block block, Location location) {

        Preconditions.checkNotNull(block, "Block cannot be null!");
        Preconditions.checkNotNull(location, "Location cannot be null!");

        final int x = location.getBlockX();
        final int y = location.getBlockY();
        final int z = location.getBlockZ();

        //block.getWorld().setBlockData();
        //block.getWorld().getBlockAt()

        final Chunk bukkitChunk = block.getChunk();
        final LevelChunk nmsChunk = ((CraftChunk) bukkitChunk).getHandle();

        final LevelChunkSection section = nmsChunk.getSection(nmsChunk.getSectionIndex(location.getBlockY()));

        // Update block state
        section.acquire();
        try {
            section.setBlockState(x, y, z, ((CraftBlockState) block.getState()).getHandle(), false); // TODO: lock would be performed ourselves for multiple set block operations
        } finally {
            section.release();
        }

        // TODO: finish

    }

    @Deprecated
    public void fastSetBlocks_OLD(Set<Block> blocks, Location location, Location origin) {

        // TODO: reconsider this
        Preconditions.checkNotNull(blocks, "Blocks cannot be null!");
        Preconditions.checkNotNull(location, "Location cannot be null!");
        Preconditions.checkNotNull(origin, "Origin cannot be null!");

        // TODO: finish

        // Goal here is to set multiple blocks from a collection at the desired location (origin is an offset from 0,0 [supposedly])
        // Get all affected chunk sections (for each block, get transformed position and add section to Map<LevelChunkSection, Set<Block>>)
        // For chunk section, acquire() and loop through blocks to place, and finally release()
        // Send Section Update Packets

        // Get affected sections
        Map<LevelChunkSection, Set<Block>> affectedSections = new HashMap<>();
        for(Block block : blocks) {

            LevelChunk chunk = ((CraftChunk) block.getChunk()).getHandle();
            LevelChunkSection section = chunk.getSection(chunk.getSectionIndex(location.getBlockY()));

            Set<Block> sectionBlocks = affectedSections.putIfAbsent(section, new HashSet<>());
            if(sectionBlocks == null) sectionBlocks = affectedSections.get(section);
            sectionBlocks.add(block);
        }

        // Set blocks in world chunk sections
        affectedSections.forEach((section, sectionBlocks) -> {

            // For packet handling
            ShortSet newPositions = new ShortArraySet();
            BlockState[] newStates = sectionBlocks.stream()
                    .map(block -> ((CraftBlockState) block.getState()).getHandle())
                    .toArray(BlockState[]::new);

            // Update blocks in world
            section.acquire();
            try {
                // Apply blocks
                for(Block block : sectionBlocks) {

                    final int x = location.getBlockX() & 15;
                    final int y = location.getBlockY() & 15;
                    final int z = location.getBlockZ() & 15;

                    section.setBlockState(x, y, z, ((CraftBlockState) block.getState()).getHandle(), false);
                }
            } finally {
                section.release();
            }

            // Send section update packets
            //SectionPos sectionPos = SectionPos.of();
            //ClientboundSectionBlocksUpdatePacket packet = new ClientboundSectionBlocksUpdatePacket();
        });
    }

    @Override
    public void fastSetBlocks(Set<Block> blocks, Location location, Location origin) {

        // TODO: switch from Set<Block> to appropriate data structure for holding block state data
        // TODO: once schematic block state data structure is resolved, fix maths with location/origin/etc.

        // Get affected sections and partition blocks
        Map<LevelChunkSection, ChunkSectionChanges> allChanges = new HashMap<>();
        for(Block block : blocks) {

            LevelChunk chunk = ((CraftChunk) block.getChunk()).getHandle();
            LevelChunkSection section = chunk.getSection(chunk.getSectionIndex(block.getY()));

            SectionPos pos = SectionPos.of(block.getX(), block.getY(), block.getZ());
            ChunkSectionChanges sectionChanges = allChanges.computeIfAbsent(section, (ignore) -> new ChunkSectionChanges(pos, chunk));

            sectionChanges.blocks.add(block);
        }

        // Apply changes to sections and broadcast
        for(Map.Entry<LevelChunkSection, ChunkSectionChanges> entry : allChanges.entrySet()) {
            final LevelChunkSection section = entry.getKey();
            final ChunkSectionChanges sectionChanges = entry.getValue();

            // Apply blocks and compute changes for packet
            applyBlocksToSection(section, sectionChanges.blocks);
            sectionChanges.computeSectionChanges();

            // Broadcast section update packet
            ClientboundSectionBlocksUpdatePacket packet = new ClientboundSectionBlocksUpdatePacket(sectionChanges.sectionPos, sectionChanges.positions, sectionChanges.states, false);
            ChunkHolder chunkHolder = sectionChanges.parentChunk.getChunkHolder().vanillaChunkHolder;
            chunkHolder.broadcast(packet, false);
        }
    }

    private void applyBlocksToSection(LevelChunkSection section, Set<Block> blocks) {
        section.acquire();
        try {
            for(Block block : blocks) {

                final int x = block.getX() & 15;
                final int y = block.getY() & 15;
                final int z = block.getZ() & 15;

                section.setBlockState(x, y, z, ((CraftBlockState) block.getState()).getHandle(), false);
            }
        } finally {
            section.release();
        }
    }

    private static class ChunkSectionChanges {

        public final Set<Block> blocks; // block states to be applied in section
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
        public void computeSectionChanges() {
            short[] positions = new short[blocks.size()];
            BlockState[] states = new BlockState[blocks.size()];

            int index = 0;
            for(Iterator<Block> it = blocks.iterator(); it.hasNext(); index++) {
                Block block = it.next();
                BlockPos pos = new BlockPos(block.getX(), block.getY(), block.getZ());

                positions[index] = SectionPos.sectionRelativePos(pos);
                states[index] = ((CraftBlockState) block.getState()).getHandle();
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
    private HashSet<ChunkPos> getNeighboringChunks(Chunk chunk) {
        return getNeighboringChunks(chunk, chunk);
    }

    // Get the neighboring chunks between two endpoints (chunks)
    private HashSet<ChunkPos> getNeighboringChunks(Chunk e1, Chunk e2) {

        if(e1.getWorld() != e2.getWorld()) {
            throw new IllegalArgumentException("Chunks are not in the same world!");
        }

        int capacity = (e2.getZ() - e1.getZ() + 3) * (e2.getX() - e1.getX() + 3);
        HashSet<ChunkPos> chunks = new HashSet<>(capacity);
        for(int x = e1.getX() - 1; x <= e2.getX() + 1; x++) {
            for(int z = e1.getZ() - 1; z <= e2.getZ() + 1; z++) {
                chunks.add(new ChunkPos(x, z));
                //System.out.println(String.format("%d, %d", x, z)); // debug
            }
        }

        return chunks;
    }
}
