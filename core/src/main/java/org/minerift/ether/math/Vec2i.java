package org.minerift.ether.math;

import java.io.Serializable;
import java.util.Objects;

// Immutable by default (use Vec2i.Mutable for mutable operations)
public class Vec2i implements Vec2, Serializable {

    public final static Vec2i ZERO = new Vec2i(0, 0);

    protected int x, z;

    public static Vec2i fromString(String str) {
        return Maths.strToVec2i(str);
    }

    public Vec2i(int x, int z) {
        this.x = x;
        this.z = z;
    }

    @Override
    public Vec2i copy() {
        return new Vec2i(x, z);
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getZ() {
        return z;
    }

    @Override
    public double getXd() {
        return 0;
    }

    @Override
    public double getZd() {
        return 0;
    }

    @Override
    public int[] getXZ() {
        return new int[]{ x, z };
    }

    @Override
    public double[] getXZd() {
        return new double[]{ x, z };
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

        @Override
        public boolean isMutable() {
            return true;
        }

        @Override
        public Vec2i.Mutable copy() {
            return new Vec2i.Mutable(x, z);
        }

        public void setX(int x) {
            super.x = x;
        }

        public void setZ(int z) {
            super.z = z;
        }

        public void set(int x, int z) {
            super.x = x;
            super.z = z;
        }

        public void add(int x, int z) {
            super.x += x;
            super.z += z;
        }

        public void add(Vec2 addend) {
            add(addend.getX(), addend.getZ());
        }

        public void subtract(int x, int z) {
            super.x -= x;
            super.z -= z;
        }

        public void subtract(Vec2 subtrahend) {
            subtract(subtrahend.getX(), subtrahend.getZ());
        }

        public Vec2i immutable() {
            return new Vec2i(getX(), getZ());
        }
    }
}