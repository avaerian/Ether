package org.minerift.ether.schematic.data;

import org.minerift.ether.math.Vec3i;

public enum Array3DOrder {
    /*XYZ() { // TODO
        @Override
        public int flatten(int x, int y, int z) {
            return 0;
        }
    },*/

    YZX() {

        @Override
        public int flatten(int width, int length, int x, int y, int z) {
            return x + z * width + y * width * length;
        }
    },

    ;

    // TODO: refactor so width and length also include height (all dimensions)
    public abstract int flatten(int width, int length, int x, int y, int z);
    public int flatten(int width, int length, Vec3i vec) {
        return flatten(width, length, vec.getX(), vec.getY(), vec.getZ());
    }
}
