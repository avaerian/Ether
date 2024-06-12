package org.minerift.ether.math;

import com.google.common.base.Preconditions;

import java.util.function.DoubleUnaryOperator;

// Immutable (by default) Vec3 of doubles
public class Vec3d {

    protected double x, y, z;

    public static Vec3d fromString(String str) {
        return Maths.strToVec3d(str);
    }
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

        public void transform(DoubleUnaryOperator x, DoubleUnaryOperator y, DoubleUnaryOperator z) {
            this.x = x.applyAsDouble(this.x);
            this.y = y.applyAsDouble(this.y);
            this.z = z.applyAsDouble(this.z);
        }

        public void add(Vec3d addend) {
            add(addend.x, addend.y, addend.z);
        }

        public void add(Vec3i addend) {
            add(addend.x, addend.y, addend.z);
        }

        public void subtract(Vec3d subtrahend) {
            subtract(subtrahend.x, subtrahend.y, subtrahend.z);
        }

        public void subtract(Vec3i subtrahend) {
            subtract(subtrahend.x, subtrahend.y, subtrahend.z);
        }

        public void add(double x1, double y1, double z1) {
            transform(
                    x -> x + x1,
                    y -> y + y1,
                    z -> z + z1
            );
        }

        public void subtract(double x1, double y1, double z1) {
            transform(
                    x -> x - x1,
                    y -> y - y1,
                    z -> z - z1
            );
        }

        public Vec3d immutable() {
            return new Vec3d(x, y, z);
        }

    }
}
