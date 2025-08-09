package org.minerift.ether.schematic.transform;

import org.minerift.ether.math.Vec3i;
import org.minerift.ether.schematic.data.Array3DOrder;

public enum Axis {
    X() { // YZ
        @Override
        int flatten(Array3DOrder order, int width, int len, int rotX, int layer, int rotZ) {
            return order.flatten(width, len, layer, rotX, rotZ);
        }
    },

    Y() { // XZ
        @Override
        int flatten(Array3DOrder order, int width, int len, int rotX, int layer, int rotZ) {
            return order.flatten(width, len, rotX, layer, rotZ);
        }
    },

    Z() { // XY
        @Override
        int flatten(Array3DOrder order, int width, int len, int rotX, int layer, int rotZ) {
            return order.flatten(width, len, rotX, rotZ, layer);
        }
    };

    //abstract int flatten(Array3DOrder order, Vec3i dim, int rotX, int rotZ, int layer);

    // TODO: move these functions out of this enum?
    int getLayers(Vec3i dim) {
        return switch (this) {
            case X -> dim.getX(); // YZ
            case Y -> dim.getY(); // XZ
            case Z -> dim.getZ(); // XY
        };
    }

    int getRotatingX(Vec3i dim) {
        return switch (this) {
            case X -> dim.getY(); // YZ
            case Y -> dim.getX(); // XZ
            case Z -> dim.getX(); // XY
        };
    }

    int getRotatingZ(Vec3i dim) {
        return switch (this) {
            case X -> dim.getZ(); // YZ
            case Y -> dim.getZ(); // XZ
            case Z -> dim.getY(); // XY
        };
    }

    // NOTE: only flattening for axis on square grid
    int flatten(Array3DOrder order, int len, int rotX, int layer, int rotZ) {
        return flatten(order, len, len, rotX, layer, rotZ);
    }
    abstract int flatten(Array3DOrder order, int width, int length, int rotX, int layer, int rotZ);

}
