package org.minerift.ether.math;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import java.util.function.BooleanSupplier;
import java.util.function.IntUnaryOperator;

// TODO: ensure serialization works for both mutable and immutable types
// Immutable (by default) Vec3 of ints
public class Vec3i {

    public static final Vec3i ZERO = new Vec3i(0, 0, 0);

    protected int x, y, z;

    public static Vec3i fromString(String str) {
        return Maths.strToVec3i(str);
    }

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

    public Vec3i copy() {
        return new Vec3i(x, y, z);
    }

    public Mutable asMutable() {
        return this instanceof Mutable ? (Mutable) this : new Mutable(this);
    }

    public boolean isGreaterThan(Vec3i other, boolean orEqualTo) {

        if(equals(other)) return orEqualTo;

        return greaterThanCheckChained(x, other.x,
                () -> greaterThanCheckChained(y, other.y,
                        () -> greaterThanCheckChained(z, other.z, null)
                )
        );
    }

    public boolean isLessThan(Vec3i other, boolean orEqualTo) {

        if(equals(other)) return orEqualTo;

        return lessThanCheckChained(x, other.x,
                () -> lessThanCheckChained(y, other.y,
                        () -> lessThanCheckChained(z, other.z, null)
                )
        );
    }

    private boolean greaterThanCheckChained(int i, int other, BooleanSupplier chain) {
        // If major is greater, return true
        // If major is less than, return false
        // Else, majors are equal -> continue down chain
        if(i > other) return true;
        if(i < other) return false;
        return chain == null ? false : chain.getAsBoolean();
    }

    private boolean lessThanCheckChained(int i, int other, BooleanSupplier chain) {
        // If major is less than, return true
        // If major is greater, return false
        // Else, majors are equal -> continue down chain
        if(i < other) return true;
        if(i > other) return false;
        return chain == null ? false : chain.getAsBoolean();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vec3i vec3i = (Vec3i) o;
        return x == vec3i.x && y == vec3i.y && z == vec3i.z;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(x, y, z);
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

        public Mutable(int[] xyz) {
            super(xyz);
        }

        public Vec3i.Mutable setX(int x) {
            this.x = x;
            return this;
        }

        public Vec3i.Mutable setY(int y) {
            this.y = y;
            return this;
        }

        public Vec3i.Mutable setZ(int z) {
            this.z = z;
            return this;
        }

        public Vec3i.Mutable transform(IntUnaryOperator x, IntUnaryOperator y, IntUnaryOperator z) {
            this.x = x.applyAsInt(this.x);
            this.y = y.applyAsInt(this.y);
            this.z = z.applyAsInt(this.z);
            return this;
        }

        public Vec3i.Mutable set(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
            return this;
        }

        public Vec3i.Mutable add(Vec3i addend) {
            return add(addend.x, addend.y, addend.z);
        }

        public Vec3i.Mutable subtract(Vec3i subtrahend) {
            return subtract(subtrahend.x, subtrahend.y, subtrahend.z);
        }

        public Vec3i.Mutable add(int x1, int y1, int z1) {
            return transform(
                    x -> x + x1,
                    y -> y + y1,
                    z -> z + z1
            );
        }

        public Vec3i.Mutable subtract(int x1, int y1, int z1) {
            return transform(
                    x -> x - x1,
                    y -> y - y1,
                    z -> z - z1
            );
        }

        public Vec3i newImmutable() {
            return new Vec3i(x, y, z);
        }
    }
}
