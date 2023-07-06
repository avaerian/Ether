package org.minerift.ether.util.math;

import com.google.common.base.Preconditions;

// Immutable (by default) Vec3 of doubles
public class Vec3d {

    protected double x, y, z;

    public Vec3d(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vec3d(double[] xyz) {
        Preconditions.checkArgument(xyz.length == 3, "Array has to have 3 coordinates!");
        this.x = xyz[0];
        this.y = xyz[1];
        this.z = xyz[2];
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public Mutable asMutable() {
        return this instanceof Mutable ? (Mutable) this : new Mutable(this);
    }

    @Override
    public String toString() {
        return String.format("(%f, %f, %f)", x, y, z);
    }

    public static class Mutable extends Vec3d {
        public Mutable(double x, double y, double z) {
            super(x, y, z);
        }

        // Use asMutable() instead
        private Mutable(Vec3d original) {
            this(original.x, original.y, original.z);
        }

        public void setX(double x) {
            this.x = x;
        }

        public void setY(double y) {
            this.y = y;
        }

        public void setZ(double z) {
            this.z = z;
        }

        public Vec3d immutable() {
            return new Vec3d(x, y, z);
        }

    }
}
