package org.minerift.ether.nms.world;

import org.bukkit.World;
import org.minerift.ether.Ether;

import java.util.function.Consumer;

public interface ChunkGetter {

    void accept(World world, int chunkX, int chunkZ, Consumer<Chunk> chunkCallback);

    ChunkGetter SYNC = (world, chunkX, chunkZ, chunkCallback) -> {
        org.bukkit.Chunk bukkitChunk = world.getChunkAt(chunkX, chunkZ);
        Chunk chunk = Chunk.of(bukkitChunk);
        chunkCallback.accept(chunk);
    };

    ChunkGetter ASYNC = (world, chunkX, chunkZ, chunkCallback) -> {
        world.getChunkAtAsync(chunkX, chunkZ)
                .thenApply(Chunk::of)
                .thenAccept(chunkCallback);
    };

}
