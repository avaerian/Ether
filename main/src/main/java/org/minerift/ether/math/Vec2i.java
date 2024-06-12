package org.minerift.ether.math;

import org.minerift.ether.world.ChunkCoords;

import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

// Immutable by default (use Vec2i.Mutable for mutable operations)
public class Vec2i {

    public final static Vec2i ZERO = new Vec2i(0, 0);

    protected int x, z;

    public static Vec2i fromString(String str) {
        return Maths.strToVec2i(str);
    }

    public Vec2i(int x, int z) {
        this.x = x;
        this.z = z;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public int getTileId() {
        return GridAlgorithm.computeTileId(this);
    }

    // TODO: refactor to return double for precision?
    public int distanceTo(Vec2i tile) {
        int distX = (x - tile.getX()) * (x - tile.getX());
        int distZ = (z - tile.getZ()) * (z - tile.getZ());
        return (int) Math.sqrt(distX + distZ);
    }

    public Vec2i.Mutable asMutable() {
        return (this instanceof Vec2i.Mutable) ? (Vec2i.Mutable) this : new Vec2i.Mutable(x, z);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vec2i tile = (Vec2i) o;
        return x == tile.x && z == tile.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, z);
    }

    @Override
    public String toString() {
        return "(" + x + ", " + z + ")";
    }

    public static class Mutable extends Vec2i {

        public Mutable(int x, int z) {
            super(x, z);
        }

        public void setX(int x) {
            super.x = x;
        }

        public void setZ(int z) {
            super.z = z;
        }

        public void add(int x, int z) {
            super.x += x;
            super.z += z;
        }

        public void subtract(int x, int z) {
            super.x -= x;
            super.z -= z;
        }

        public Vec2i immutable() {
            return new Vec2i(getX(), getZ());
        }
    }
}