package org.minerift.ether.nms.v1_20_R2;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import org.minerift.ether.world.BlockArchetype;
import org.minerift.ether.world.ChunkCoords;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import static java.lang.Math.max;
import static java.lang.Math.min;

// Doesn't load chunks.
// Benefit to this implementation is when blocks are being partitioned
// into sections and chunks, the chunks aren't loaded yet.
// When setting blocks from the partition, chunks should be loaded
public class BlockPartition { // TODO: move this to core

    // Chunk, Section, ChunkSectionChanges
    private final Map<ChunkCoords, Int2ObjectMap<ChunkSectionChanges>> partition;
    private int expectedBlockCount; // expected size for partition; used for allocating when partitioning section changes
    private final int minWorldHeight;
    private ChunkCoords bottomLeft, topRight; // region bounds

    public BlockPartition(List<BlockArchetype> blocks, int minWorldHeight) {
        this.minWorldHeight = minWorldHeight;
        this.partition = new HashMap<>();
        addAll(blocks);
    }

    public void add(BlockArchetype block) {
        int initialChunkCount = partition.size();

        // Partition block
        expectedBlockCount++;
        partitionSingleBlock(block);

        // Recalculate bounds if needed
        if(partition.size() != initialChunkCount) {
            recalcRegionBounds();
            resetPartitionCache(); // if partitioned chunks are added, reset cache
        }
    }

    // Preferred for adding multiple blocks as calls are reduced
    public void addAll(Collection<BlockArchetype> blocks) {
        int initialChunkCount = partition.size();

        // Partition blocks
        expectedBlockCount += blocks.size();
        for(BlockArchetype block : blocks) {
            partitionSingleBlock(block);
        }

        resetPartitionCache(); // after updating multiple blocks, reset cache

        // Recalculate bounds if needed
        if(partition.size() != initialChunkCount) {
            recalcRegionBounds();
        }
    }

    public int getChunkCount() {
        return partition.size();
    }

    public Set<ChunkCoords> getChunks() {
        return partition.keySet();
    }

    public int getSectionCount(ChunkCoords chunk) {
        return partition.getOrDefault(chunk, Int2ObjectMaps.emptyMap()).size();
    }

    public int getSectionTotalCount() {
        int total = 0;
        for(Int2ObjectMap<ChunkSectionChanges> sections : partition.values()) {
            total += sections.size();
        }
        return total;
    }

    public Int2ObjectMap<ChunkSectionChanges> getSections(ChunkCoords chunk) {
        return partition.get(chunk);
    }

    public int getBlockCount() {
        int total = 0;
        for(var section : partition.values()) {
            for(ChunkSectionChanges sectionChanges : section.values()) {
                total += sectionChanges.blocks.size();
            }
        }
        return total;
    }

    public void recalcRegionBounds() {
        int blX, blZ, trX, trZ;
        blX = blZ = Integer.MAX_VALUE; // this value should never be reached
        trX = trZ = Integer.MIN_VALUE; // same here

        // Iterate chunks and find bounds
        for(ChunkCoords chunkCoords : partition.keySet()) {
            blX = min(blX, chunkCoords.x);
            blZ = min(blZ, chunkCoords.z);
            trX = max(trX, chunkCoords.x);
            trZ = max(trZ, chunkCoords.z);
        }

        this.bottomLeft = new ChunkCoords(blX, blZ);
        this.topRight = new ChunkCoords(trX, trZ);
    }

    public Stream<ChunkCoords> getChunksForRelighting() {
        return partition.isEmpty()
                ? Stream.empty()
                : ChunkCoords.getNeighboringChunks(bottomLeft, topRight);
    }

    public void forEach(BiConsumer<ChunkCoords, Int2ObjectMap<ChunkSectionChanges>> callback) {
        partition.forEach(callback);
    }

    private void resetPartitionCache() {
        this.lastChunkKey = -1;
        this.lastSectionIdx = -1;
        this.lastSectionChanges = null;
    }

    // Simple cache for partition method
    private long lastChunkKey = -1;
    private int lastSectionIdx = -1;
    private ChunkSectionChanges lastSectionChanges = null;
    private void partitionSingleBlock(BlockArchetype block) {
        // math for this pulled from LevelHeightAccessor; simplified
        //int section = SectionPos.blockToSectionCoord(block.getY()) - SectionPos.blockToSectionCoord(world.getMinHeight());
        int sectionIdx = (block.getY() >> 4) - (minWorldHeight >> 4);

        ChunkSectionChanges sectionChanges;
        if(lastChunkKey == ChunkCoords.getChunkKey(block.getChunkX(), block.getChunkZ()) && lastSectionIdx == sectionIdx) {
            sectionChanges = lastSectionChanges;
        } else {
            ChunkCoords chunk = new ChunkCoords(block.getChunkX(), block.getChunkZ());
            lastChunkKey = chunk.getChunkKey();

            Int2ObjectMap<ChunkSectionChanges> chunkChanges = partition.computeIfAbsent(chunk, (ignore) -> new Int2ObjectArrayMap<>());
            sectionChanges = chunkChanges.computeIfAbsent(sectionIdx, (ignore) -> new ChunkSectionChanges(sectionIdx, expectedBlockCount));
        }

        // Partition block
        sectionChanges.blocks.add(block);

        // Update simple cache
        lastSectionIdx = sectionIdx;
        lastSectionChanges = sectionChanges;
    }
}
