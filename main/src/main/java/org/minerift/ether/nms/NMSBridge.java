package org.minerift.ether.nms;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.minerift.ether.world.BlockArchetype;
import org.minerift.ether.world.EntityArchetype;

import java.util.List;

public interface NMSBridge {

    // Clear a single chunk
    void fastClearChunk(Chunk chunk, boolean clearEntities);

    // Clear a range of chunks from two endpoints
    void fastClearChunks(Chunk e1, Chunk e2, boolean clearEntities);

    // Clears a range of chunks from two endpoints asynchronously
    void fastClearChunksAsync(Chunk e1, Chunk e2, boolean clearEntities);

    void fastSetBlocks(List<BlockArchetype> blocks, World world);

    void fastSetBlocksAsync(List<BlockArchetype> blocks, World world);

    void fastSetBlocksAsyncLazy(List<BlockArchetype> blocks, World world);

    Entity spawnEntity(EntityArchetype entity, World world);

    void testIslandScanIdea(Location location);

    void testIslandScanIdeaFullChunk(Location location);

    void testIslandScanIdeaMultiChunk(Location location, int diameter);

}
