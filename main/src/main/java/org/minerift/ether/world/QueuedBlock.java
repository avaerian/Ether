package org.minerift.ether.world;

import org.bukkit.block.data.BlockData;
import org.minerift.ether.util.math.Vec3i;

import java.util.function.Function;

// Represents a single block ready for world placement
public class QueuedBlock {

    private Vec3i.Mutable pos;
    private BlockData data;

    public QueuedBlock(Vec3i pos, BlockData data) {
        this.pos = pos.asMutable();
        this.data = data;
    }

    public Vec3i.Mutable getPos() {
        return pos;
    }

    public BlockData getData() {
        return data;
    }

    public int getX() {
        return pos.getX();
    }

    public int getY() {
        return pos.getY();
    }

    public int getZ() {
        return pos.getZ();
    }

    public int getChunkX() {
        return getX() >> 4;
    }

    public int getChunkZ() {
        return getZ() >> 4;
    }

    public <R> R getState(Function<BlockData, R> dataToStateFunction) {
        return dataToStateFunction.apply(data);
    }

    @Override
    public String toString() {
        return String.format("%s -> %s", pos, data);
    }
}
