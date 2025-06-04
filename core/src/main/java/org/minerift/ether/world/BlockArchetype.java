package org.minerift.ether.world;

import org.minerift.ether.math.Vec3i;

// Represents a single block ready for world placement
public class BlockArchetype implements Archetype {

    protected final String data;
    protected final Vec3i.Mutable pos;

    public BlockArchetype(Vec3i pos, String data) {
        this.pos = pos.asMutable();
        this.data = data;
    }

    public String getData() {
        return data;
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
        return String.format("%s -> %s", pos, data);
    }
}
