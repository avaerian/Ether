package org.minerift.ether.schematic.data;

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

    public abstract int flatten(int width, int length, int x, int y, int z);
}
