package org.minerift.ether.math;

import java.io.Serializable;
import java.util.Objects;

// Immutable by default (use Vec2i.Mutable for mutable operations)
public class Vec2i implements Vec2, Serializable {

    public static Vec2i fromString(String str) {
        return Maths.strToVec2i(str);
    }

    public static Vec2i min(Vec2i v1, Vec2i v2) {
        return new Vec2i(Math.min(v1.x, v2.x), Math.min(v1.z, v2.z));
    }

    public static Vec2i max(Vec2i v1, Vec2i v2) {
        return new Vec2i(Math.max(v1.x, v2.x), Math.max(v1.z, v2.z));
    }

    public static boolean isInside(Vec2 from, Vec2 to, Vec2 test) {
        Vec2i _from = from.asVec2i();
        Vec2i _to = to.asVec2i();

        Vec2i min = Vec2i.min(_from, _to);
        Vec2i max = Vec2i.max(_from, _to);
        return min.getX() <= test.getX() &&
                min.getZ() <= test.getZ() &&
                max.getX() >= test.getX() &&
                max.getZ() >= test.getZ();
    }

    public final static Vec2i ZERO = new Vec2i(0, 0);

    protected int x, z;

    public Vec2i(int x, int z) {
        this.x = x;
        this.z = z;
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
        return x;
    }

    @Override
    public double getZd() {
        return z;
    }

    @Override
    public long getXl() {
        return x;
    }

    @Override
    public long getZl() {
        return z;
    }

    @Override
    public int[] getXZ() {
        return new int[]{ x, z };
    }

    @Override
    public double[] getXZd() {
        return new double[]{ x, z };
    }

    @Override
    public long[] getXZl() {
        return new long[]{ x, z };
    }

    @Override
    public Vec2i asVec2i() {
        return this;
    }

    @Override
    public Vec2d asVec2d() {
        return new Vec2d(x, z);
    }

    @Override
    public Vec2l asVec2l() {
        return new Vec2l(x, z);
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

    @Override
    public Vec2i copy() {
        return new Vec2i(x, z);
    }

    public Vec2i.Mutable asMutable() {
        return isMutable() ? (Vec2i.Mutable) this : new Vec2i.Mutable(x, z);
    }

    public Vec2i copyImmutable() {
        return new Vec2i(x, z);
    }

    public Vec2i.Mutable copyMutable() {
        return new Vec2i.Mutable(x, z);
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

        public void setX(int x) {
            this.x = x;
        }

        public void setZ(int z) {
            this.z = z;
        }

        public void set(Vec2 vec) {
            set(vec.getX(), vec.getZ());
        }

        public void set(int x, int z) {
            this.x = x;
            this.z = z;
        }

        public void add(int x, int z) {
            this.x += x;
            this.z += z;
        }

        public void add(Vec2 addend) {
            add(addend.getX(), addend.getZ());
        }

        public void subtract(int x, int z) {
            this.x -= x;
            this.z -= z;
        }

        public void subtract(Vec2 subtrahend) {
            subtract(subtrahend.getX(), subtrahend.getZ());
        }

        @Override
        public Vec2i.Mutable copy() {
            return new Vec2i.Mutable(x, z);
        }

        public Vec2i immutable() {
            return new Vec2i(x, z);
        }
    }
}