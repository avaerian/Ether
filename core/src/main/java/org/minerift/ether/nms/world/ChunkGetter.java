package org.minerift.ether.nms.world;

import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.function.UnaryOperator;

import static java.util.concurrent.CompletableFuture.completedFuture;

public enum ChunkGetter {

    // NOTE: ChunkGetter shouldn't need to store any data; it only provides abstract methods

    SYNC {
        public CompletableFuture<Chunk> getChunkWCallback(World world, int cx, int cz, @NotNull UnaryOperator<Chunk> callback) {
            org.bukkit.Chunk bukkitChunk = world.getChunkAt(cx, cz, true);
            Chunk chunk = Chunk.of(bukkitChunk);
            callback.apply(chunk);
            return completedFuture(chunk);
        }

        @Override
        public CompletableFuture<Chunk> getChunk(World world, int cx, int cz) {
            return completedFuture(Chunk.of(world.getChunkAt(cx, cz)));
        }
    },

    ASYNC {
        public CompletableFuture<Chunk> getChunkWCallback(World world, int cx, int cz, @NotNull UnaryOperator<Chunk> callback) {
            return world.getChunkAtAsync(cx, cz, true)
                    .thenApply(Chunk::of)
                    .thenApply(callback);
        }

        @Override
        public CompletableFuture<Chunk> getChunk(World world, int cx, int cz) {
            return world.getChunkAtAsync(cx, cz, true)
                    .thenApply(Chunk::of);
        }
    },

    ;

    public abstract CompletableFuture<Chunk> getChunkWCallback(World world, int cx, int cz, @NotNull UnaryOperator<Chunk> callback);

    public abstract CompletableFuture<Chunk> getChunk(World world, int cx, int cz);


}
