package org.minerift.ether.nms;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

import java.util.Set;

public interface NMSBridge {

    // Clear a single chunk
    void fastClearChunk(Chunk chunk, boolean clearEntities);

    // Clear a range of chunks from two endpoints
    void fastClearChunks(Chunk e1, Chunk e2, boolean clearEntities);

    // Clears a range of chunks from two endpoints semi-asynchronously
    void fastClearChunksAsync(Chunk e1, Chunk e2, boolean clearEntities);

    void fastSetBlock(Block block, Location location);

    //
    void fastSetBlocks(Set<Block> blocks, Location location, Location origin);

}
