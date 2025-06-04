package org.minerift.ether.math;

import com.google.common.base.Preconditions;

import java.io.Serializable;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleUnaryOperator;

// Immutable (by default) Vec3 of doubles
public class Vec3d implements Vec3<Vec3d>, Serializable {

    public static final Vec3d ZERO = new Vec3d(0, 0, 0);

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

    @Override
    public Vec3d copy() {
        return new Vec3d(x, y, z);
    }

    @Override
    public int getX() {
        return (int)x;
    }

    @Override
    public int getY() {
        return (int)y;
    }

    @Override
    public int getZ() {
        return (int)z;
    }


    @Override
    public double getXd() {
        return x;
    }

    @Override
    public double getYd() {
        return y;
    }

    @Override
    public double getZd() {
        return z;
    }

    @Override
    public Vec3d asVec3d() {
        return this;
    }

    @Override
    public Vec3i asVec3i() {
        return new Vec3i((int)x, (int)y, (int)z);
    }

    public Mutable asMutable() {
        return this instanceof Mutable ? (Mutable) this : new Mutable(this);
    }


    // TODO: Specifically for versioning; need to better understand this
    public boolean isGreaterThan(Vec3d other, boolean orEqualTo) {

        if(equals(other)) return orEqualTo;

        return greaterThanCheckChained(x, other.x,
                () -> greaterThanCheckChained(y, other.y,
                        () -> greaterThanCheckChained(z, other.z, null)
                )
        );
    }

    // TODO: Specifically for versioning; need to better understand this
    public boolean isLessThan(Vec3d other, boolean orEqualTo) {

        if(equals(other)) return orEqualTo;

        return lessThanCheckChained(x, other.x,
                () -> lessThanCheckChained(y, other.y,
                        () -> lessThanCheckChained(z, other.z, null)
                )
        );
    }

    private boolean greaterThanCheckChained(double i, double other, BooleanSupplier chain) {
        // If major is greater, return true
        // If major is less than, return false
        // Else, majors are equal -> continue down chain
        if(i > other) return true;
        if(i < other) return false;
        return chain == null ? false : chain.getAsBoolean();
    }

    private boolean lessThanCheckChained(double i, double other, BooleanSupplier chain) {
        // If major is less than, return true
        // If major is greater, return false
        // Else, majors are equal -> continue down chain
        if(i < other) return true;
        if(i > other) return false;
        return chain == null ? false : chain.getAsBoolean();
    }

    @Override
    public String toString() {
        return String.format("(%f, %f, %f)", x, y, z);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vec3d vec3d = (Vec3d) o;
        return x == vec3d.x && y == vec3d.y && z == vec3d.z;
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
            /*transform(
                    x -> x + x1,
                    y -> y + y1,
                    z -> z + z1
            );*/
            this.x += x1;
            this.y += y1;
            this.z += z1;
        }

        public void subtract(double x1, double y1, double z1) {
            /*transform(
                    x -> x - x1,
                    y -> y - y1,
                    z -> z - z1
            );*/
            this.x -= x1;
            this.y -= y1;
            this.z -= z1;
        }

        public Vec3d immutable() {
            return new Vec3d(x, y, z);
        }

    }
}
