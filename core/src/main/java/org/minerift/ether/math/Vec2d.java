package org.minerift.ether.math;

import com.google.common.base.Preconditions;

public class Vec2d implements Vec2 {

    public static Vec2d min(Vec2d v1, Vec2d v2) {
        return new Vec2d(Math.min(v1.x, v2.x), Math.min(v1.z, v2.z));
    }

    public static Vec2d max(Vec2d v1, Vec2d v2) {
        return new Vec2d(Math.max(v1.x, v2.x), Math.max(v1.z, v2.z));
    }

    public static boolean isInside(Vec2 from, Vec2 to, Vec2 test) {
        Vec2d _from = from.asVec2d();
        Vec2d _to = to.asVec2d();

        Vec2d min = Vec2d.min(_from, _to);
        Vec2d max = Vec2d.max(_from, _to);
        return min.getXd() <= test.getXd() &&
                min.getZd() <= test.getZd() &&
                max.getXd() >= test.getXd() &&
                max.getZd() >= test.getZd();
    }

    public static final Vec2d ZERO = new Vec2d(0,0);

    protected double x, z;

    public Vec2d(double x, double z) {
        this.x = x;
        this.z = z;
    }

    public Vec2d(double[] xz) {
        Preconditions.checkArgument(xz.length == 2, "Expected 2 array elements, found " + xz.length);
        this.x = xz[0];
        this.z = xz[1];
    }

    @Override
    public int getX() {
        return (int) x;
    }

    @Override
    public int getZ() {
        return (int) z;
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
        return (long) x;
    }

    @Override
    public long getZl() {
        return (long) z;
    }

    @Override
    public int[] getXZ() {
        return new int[] { getX(), getZ() };
    }

    @Override
    public double[] getXZd() {
        return new double[] { x, z };
    }

    @Override
    public long[] getXZl() {
        return new long[] {(long) x, (long) z};
    }

    @Override
    public Vec2i asVec2i() {
        return new Vec2i(getX(), getZ());
    }

    @Override
    public Vec2d asVec2d() {
        return this;
    }

    @Override
    public Vec2l asVec2l() {
        return new Vec2l(getXl(), getZl());
    }

    @Override
    public Vec2d copy() {
        return new Vec2d(x, z);
    }

    public Vec2d copyImmutable() {
        return new Vec2d(x, z);
    }

    public Vec2d.Mutable copyMutable() {
        return new Mutable(x, z);
    }

    public Vec2d.Mutable asMutable() {
        return isMutable() ? (Mutable) this : new Mutable(x, z);
    }

    public static class Mutable extends Vec2d /*implements Vec2.Mutable*/ {

        public Mutable(double x, double z) {
            super(x, z);
        }

        public Vec2d.Mutable add(double x, double z) {
            this.x += x;
            this.z += z;
            return this;
        }

        public Vec2d.Mutable add(Vec2 vec) {
            this.x += vec.getXd();
            this.z += vec.getZd();
            return this;
        }

        public Vec2d.Mutable subtract(double x, double z) {
            this.x -= x;
            this.z -= z;
            return this;
        }

        public Vec2d.Mutable subtract(Vec2 vec) {
            this.x -= vec.getXd();
            this.z -= vec.getZd();
            return this;
        }

        public Vec2d.Mutable multiply(Vec2 minuend) {
            this.x *= minuend.getXd();
            this.z *= minuend.getZd();
            return this;
        }

        public Vec2d.Mutable divide(Vec2 diff) {
            this.x /= diff.getXd();
            this.z /= diff.getZd();
            return this;
        }

        public Vec2d.Mutable setX(double x) {
            this.x = x;
            return this;
        }

        public Vec2d.Mutable setZ(double z) {
            this.z = z;
            return this;
        }

        public Vec2d.Mutable set(Vec2 vec) {
            return set(vec.getXd(), vec.getZd());
        }

        public Vec2d.Mutable set(double x, double z) {
            this.x = x;
            this.z = z;
            return this;
        }

        @Override
        public Vec2d.Mutable copy() {
            return new Vec2d.Mutable(x, z);
        }

        @Override
        public boolean isMutable() {
            return true;
        }
    }
}
