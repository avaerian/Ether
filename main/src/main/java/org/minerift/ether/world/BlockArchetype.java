package org.minerift.ether.world;

import org.bukkit.Bukkit;
import org.bukkit.block.data.BlockData;
import org.minerift.ether.util.math.Vec3i;

import java.util.function.Function;

// Represents a single block ready for world placement
public class BlockArchetype {

    protected final String id;
    protected final Vec3i.Mutable pos;

    public BlockArchetype(String id, Vec3i pos) {
        this.id = id;
        this.pos = pos.asMutable();
    }

    public String getId() {
        return id;
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

    @Override
    public String toString() {
        return String.format("%s -> %s", pos, id);
    }
}
