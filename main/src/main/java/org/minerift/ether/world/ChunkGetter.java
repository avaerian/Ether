package org.minerift.ether.world;

import org.bukkit.Chunk;
import org.bukkit.World;

import java.util.function.Consumer;

public interface ChunkGetter {

    void accept(World world, int chunkX, int chunkZ, Consumer<Chunk> cb);

    ChunkGetter SYNC = (world, x, z, cb) -> cb.accept(world.getChunkAt(x, z));

    ChunkGetter ASYNC = World::getChunkAtAsync;

    // Purpose of these two methods is to allow for additional chunk getter methods
    // while trying to avoid conflicts in API changes
    default boolean isAsync() {
        return this == ASYNC;
    }

    default boolean isSync() {
        return this == SYNC;
    }

    /*GetChunkFunction ASYNC = (world, x, z, chunkCallback) -> {
        world.getChunkAtAsync(x, z, chunkCallback);
    };*/
}
