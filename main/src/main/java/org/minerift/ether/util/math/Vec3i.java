package org.minerift.ether.util.math;

import com.google.common.base.Preconditions;

import java.util.function.IntUnaryOperator;

// Immutable (by default) Vec3 of ints
public class Vec3i {

    protected int x, y, z;

    public Vec3i(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vec3i(int[] xyz) {
        Preconditions.checkArgument(xyz.length == 3, "Array has to have 3 coordinates!");
        this.x = xyz[0];
        this.y = xyz[1];
        this.z = xyz[2];
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public Mutable asMutable() {
        return this instanceof Mutable ? (Mutable) this : new Mutable(this);
    }

    @Override
    public String toString() {
        return String.format("(%d, %d, %d)", x, y, z);
    }

    public static class Mutable extends Vec3i {
        public Mutable(int x, int y, int z) {
            super(x, y, z);
        }

        // Use asMutable() instead
        private Mutable(Vec3i original) {
            this(original.x, original.y, original.z);
        }

        public void setX(int x) {
            this.x = x;
        }

        public void setY(int y) {
            this.y = y;
        }

        public void setZ(int z) {
            this.z = z;
        }

        public void transform(IntUnaryOperator x, IntUnaryOperator y, IntUnaryOperator z) {
            this.x = x.applyAsInt(this.x);
            this.y = y.applyAsInt(this.y);
            this.z = z.applyAsInt(this.z);
        }

        public void add(int x1, int y1, int z1) {
            transform(
                    x -> x + x1,
                    y -> y + y1,
                    z -> z + z1
            );
        }

        public void subtract(int x1, int y1, int z1) {
            transform(
                    x -> x - x1,
                    y -> y - y1,
                    z -> z - z1
            );
        }

        public Vec3i immutable() {
            return new Vec3i(x, y, z);
        }

    }
}