package org.minerift.ether.math;

import java.util.Objects;

// Immutable by default (use Vec2i.Mutable for mutable operations)
public class Vec2i {

    public final static Vec2i ZERO = new Vec2i(0, 0);

    private int x, z;

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