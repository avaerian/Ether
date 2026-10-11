package org.minerift.ether.math;

import java.util.function.LongUnaryOperator;

public class Vec2l implements Vec2 {

    public static Vec2l min(Vec2l v1, Vec2l v2) {
        return new Vec2l(Math.min(v1.x, v2.x), Math.min(v1.z, v2.z));
    }

    public static Vec2l max(Vec2l v1, Vec2l v2) {
        return new Vec2l(Math.max(v1.x, v2.x), Math.max(v1.z, v2.z));
    }

    public static boolean isInside(Vec2 from, Vec2 to, Vec2 test) {
        Vec2l _from = from.asVec2l();
        Vec2l _to = to.asVec2l();

        Vec2l min = Vec2l.min(_from, _to);
        Vec2l max = Vec2l.max(_from, _to);
        return min.getXl() <= test.getXl() &&
                min.getZl() <= test.getZl() &&
                max.getXl() >= test.getXl() &&
                max.getZl() >= test.getZl();
    }

    public static final Vec2l ZERO = new Vec2l(0, 0);

    protected long x, z;

    public Vec2l(long x, long z) {
        this.x = x;
        this.z = z;
    }

    @Override
    public int getX() {
        return Math.toIntExact(x);
    }

    @Override
    public int getZ() {
        return Math.toIntExact(z);
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
    public double getXd() {
        return x;
    }

    @Override
    public double getZd() {
        return z;
    }

    @Override
    public int[] getXZ() {
        return new int[]{ Math.toIntExact(x), Math.toIntExact(z) };
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
        return new Vec2i(getX(), getZ());
    }

    @Override
    public Vec2d asVec2d() {
        return new Vec2d(x, z);
    }

    @Override
    public Vec2l asVec2l() {
        return this;
    }

    @Override
    public Vec2l copy() {
        return new Vec2l(x, z);
    }

    public final Vec2l copyImmutable() {
        return new Vec2l(x, z);
    }

    public final Vec2l.Mutable copyMutable() {
        return new Vec2l.Mutable(x, z);
    }

    public final Vec2l.Mutable asMutable() {
        return isMutable() ? (Mutable) this : new Vec2l.Mutable(x, z);
    }

    public static class Mutable extends Vec2l {

        public Mutable(long x, long z) {
            super(x, z);
        }

        @Override
        public boolean isMutable() {
            return true;
        }

        public Vec2l.Mutable set(long x, long z) {
            this.x = x;
            this.z = z;
            return this;
        }

        public Vec2l.Mutable setX(long x) {
            this.x = x;
            return this;
        }

        public Vec2l.Mutable setZ(long z) {
            this.z = z;
            return this;
        }

        public Vec2l.Mutable add(long x, long z) {
            this.x += x;
            this.z += z;
            return this;
        }

        public Vec2l.Mutable subtract(long x, long z) {
            this.x -= x;
            this.z -= z;
            return this;
        }

        public Vec2l.Mutable mutiply(long x, long z) {
            this.x *= x;
            this.z *= z;
            return this;
        }

        public Vec2l.Mutable divide(long x, long z) {
            this.x /= x;
            this.z /= z;
            return this;
        }

        public Vec2l.Mutable transform(LongUnaryOperator x1, LongUnaryOperator x2) {
            this.x = x1.applyAsLong(x);
            this.z = x1.applyAsLong(z);
            return this;
        }

        @Override
        public Vec2l.Mutable copy() {
            return new Vec2l.Mutable(x, z);
        }
    }

}
