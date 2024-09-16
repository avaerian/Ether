package org.minerift.ether.nms.v1_19_R3;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.minerift.ether.math.Vec3i;

import java.util.function.Consumer;

@Deprecated(forRemoval = true)
public interface DeprecatedGetChunkFunction {
    void accept(World world, Vec3i pos, Consumer<Chunk> cb);

    DeprecatedGetChunkFunction SYNC = (world, pos, chunkCallback) -> {
        chunkCallback.accept(world.getChunkAt(pos.getX() >> 4, pos.getZ() >> 4));
    };

    DeprecatedGetChunkFunction ASYNC = (world, pos, chunkCallback) -> {
        //world.loadChunk(pos.getX() >> 4, pos.getZ() >> 4); // TODO: review because this keeps the chunks loaded indefinitely
        world.getChunkAtAsync(pos.getX() >> 4, pos.getZ() >> 4, chunkCallback);
    };
}
