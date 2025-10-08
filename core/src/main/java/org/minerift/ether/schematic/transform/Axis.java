package org.minerift.ether.schematic.transform;

import org.minerift.ether.math.Vec3d;
import org.minerift.ether.math.Vec3i;
import org.minerift.ether.schematic.data.Array3DOrder;

public enum Axis {
    X() { // YZ
        @Override
        public int flatten(Array3DOrder order, int width, int len, int rotX, int layer, int rotZ) {
            return order.flatten(width, len, layer, rotX, rotZ);
        }
    },

    Y() { // XZ
        @Override
        public int flatten(Array3DOrder order, int width, int len, int rotX, int layer, int rotZ) {
            return order.flatten(width, len, rotX, layer, rotZ);
        }
    },

    Z() { // XY
        @Override
        public int flatten(Array3DOrder order, int width, int len, int rotX, int layer, int rotZ) {
            return order.flatten(width, len, rotX, rotZ, layer);
        }
    };

    //abstract int flatten(Array3DOrder order, Vec3i dim, int rotX, int rotZ, int layer);

    public Vec3i rotateDim(Vec3i dim) {
        return switch (this) {
            case X -> new Vec3i(dim.getX(), dim.getZ(), dim.getY());
            case Y -> new Vec3i(dim.getZ(), dim.getY(), dim.getX());
            case Z -> new Vec3i(dim.getY(), dim.getX(), dim.getZ());
        };
    }

    public Vec3i reorder(int rotX, int layer, int rotZ) {
        return switch (this) {
            case X -> new Vec3i(layer, rotX, rotZ);
            case Y -> new Vec3i(rotX, layer, rotZ);
            case Z -> new Vec3i(rotX, rotZ, layer);
        };
    }

    public Vec3d reorder(double rotX, double layer, double rotZ) {
        return switch (this) {
            case X -> new Vec3d(layer, rotX, rotZ);
            case Y -> new Vec3d(rotX, layer, rotZ);
            case Z -> new Vec3d(rotX, rotZ, layer);
        };
    }

    // TODO: move these functions out of this enum?
    public double getLayers(Vec3d dim) {
        return switch (this) {
            case X -> dim.getX(); // YZ
            case Y -> dim.getY(); // XZ
            case Z -> dim.getZ(); // XY
        };
    }

    public double getRotatingX(Vec3d dim) {
        return switch (this) {
            case X -> dim.getY(); // YZ
            case Y -> dim.getX(); // XZ
            case Z -> dim.getX(); // XY
        };
    }

    public double getRotatingZ(Vec3d dim) {
        return switch (this) {
            case X -> dim.getZ(); // YZ
            case Y -> dim.getZ(); // XZ
            case Z -> dim.getY(); // XY
        };
    }

    // TODO: move these functions out of this enum?
    public int getLayers(Vec3i dim) {
        return switch (this) {
            case X -> dim.getX(); // YZ
            case Y -> dim.getY(); // XZ
            case Z -> dim.getZ(); // XY
        };
    }

    public int getRotatingX(Vec3i dim) {
        return switch (this) {
            case X -> dim.getY(); // YZ
            case Y -> dim.getX(); // XZ
            case Z -> dim.getX(); // XY
        };
    }

    public int getRotatingZ(Vec3i dim) {
        return switch (this) {
            case X -> dim.getZ(); // YZ
            case Y -> dim.getZ(); // XZ
            case Z -> dim.getY(); // XY
        };
    }

    public boolean rotatesHorizontalPlane() {
        return this == Y;
    }

    public boolean rotatesVerticalPlane() {
        return this != Y;
    }

    // NOTE: only flattening for axis on square grid
    public int flatten(Array3DOrder order, int len, int rotX, int layer, int rotZ) {
        return flatten(order, len, len, rotX, layer, rotZ);
    }
    public abstract int flatten(Array3DOrder order, int width, int length, int rotX, int layer, int rotZ);

}
