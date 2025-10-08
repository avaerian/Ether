package org.minerift.ether.math;

import com.google.common.base.Preconditions;

public class Vec2d implements Vec2 {

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
    public int[] getXZ() {
        return new int[] { getX(), getZ() };
    }

    @Override
    public double[] getXZd() {
        return new double[] { x, z };
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

    public static class Mutable extends Vec2d {

        public Mutable(double x, double z) {
            super(x, z);
        }



        @Override
        public Vec2d.Mutable copy() {
            return new Mutable(x, z);
        }

        @Override
        public boolean isMutable() {
            return true;
        }
    }
}
