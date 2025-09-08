package org.minerift.ether.world;

import org.minerift.ether.math.Vec3i;
import org.minerift.ether.nms.BlockStateNotFoundException;
import org.minerift.ether.nms.world.block.BlockState;

// Represents a single block ready for world placement
public class BlockArchetype {

    protected final BlockState<?> state;
    protected final Vec3i.Mutable pos;

    public BlockArchetype(String data, Vec3i pos) throws BlockStateNotFoundException {
        this.state = BlockState.of(data);
        this.pos = pos.asMutable();
    }

    public BlockArchetype(BlockState<?> state, Vec3i pos) {
        this.state = state;
        this.pos = pos.asMutable();
    }

    public BlockState<?> getState() {
        return state;
    }

    public Vec3i.Mutable getPos() {
        return pos;
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

    public ChunkCoords getChunk() {
        return new ChunkCoords(getChunkX(), getChunkZ());
    }

    @Override
    public String toString() {
        return String.format("%s -> %s", pos, state);
    }
}
