package org.minerift.ether.nms.world;

import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface ChunkGetter {

    CompletableFuture<Chunk> accept(World world, int chunkX, int chunkZ, @Nullable Consumer<Chunk> chunkCallback);

    ChunkGetter SYNC = (world, chunkX, chunkZ, chunkCallback) -> {
        org.bukkit.Chunk bukkitChunk = world.getChunkAt(chunkX, chunkZ, true);
        Chunk chunk = Chunk.of(bukkitChunk);
        if(chunkCallback != null) {
            chunkCallback.accept(chunk);
        }
        return CompletableFuture.completedFuture(chunk);
    };

    ChunkGetter ASYNC = (world, chunkX, chunkZ, chunkCallback) -> {
        CompletableFuture<Chunk> f = world.getChunkAtAsync(chunkX, chunkZ, true)
                .thenApply(Chunk::of);
        if(chunkCallback != null) {
            f.thenAccept(chunkCallback);
        }
        return f;
    };

}
