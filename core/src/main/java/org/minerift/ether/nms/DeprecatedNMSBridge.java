package org.minerift.ether.nms;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.generator.WorldInfo;
import org.minerift.ether.math.Vec3i;
import org.minerift.ether.util.Note;
import org.minerift.ether.world.BlockArchetype;
import org.minerift.ether.world.EntityArchetype;

import java.util.List;

@Deprecated
public interface DeprecatedNMSBridge {

    NativeTypeConversions<?,?,?,?> getConverter();

    //void bootstrap();

    // Clear a single chunk
    void fastClearChunk(Chunk chunk, boolean clearEntities);

    // Clear a range of chunks from two endpoints
    void fastClearChunks(Chunk e1, Chunk e2, boolean clearEntities);

    // Clears a range of chunks from two endpoints asynchronously
    void fastClearChunksAsync(Chunk e1, Chunk e2, boolean clearEntities);

    @Note("For quick testing, need to create a more concrete implementation")
    void testSetSectionBlock(int cx, int sy, int cz, Vec3i chunkPos, Vec3i arrayPos);

    void fastSetBlocks(List<BlockArchetype> blocks, World world);

    void fastSetBlocksAsync(List<BlockArchetype> blocks, World world);

    void fastSetBlocksAsyncLazy(List<BlockArchetype> blocks, World world);

    Entity spawnEntity(EntityArchetype entity, World world);

    void testNewPartitionPaster(List<BlockArchetype> blocks, World world);

    void testIslandScanIdea(Location location);

    void testIslandScanIdeaFullChunk(Location location);

    void testIslandScanIdeaMultiChunk(Location location, int diameter);

    NamespacedKey getBiomeAt(World world, int x, int y, int z);

    @Deprecated
    default void test(World world) {
        // for testing methods from World.class
        /**
         * TODO: methods to create for new NMSBridge
         * - spawn entities
         * - getChunkAt ?
         * - getChunkAtAsync ?
         * - scan blocks
         * - world border control ? (need to investigate more)
         */
    }

}
