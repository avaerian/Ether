package org.minerift.ether.schematic.data;

import org.minerift.ether.debug.Debug;
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

        @Override
        public Vec3i unflatten(int width, int length, int flat) {
            int y = flat / (width * length);
            int z = (flat / width) % length;
            int x = flat % width;
            return new Vec3i(x,y,z);
        }
    },

    ;

    // TODO: refactor so width and length also include height (all dimensions) ??
    public abstract int flatten(int width, int length, int x, int y, int z);
    public int flatten(int width, int length, Vec3i vec) {
        return flatten(width, length, vec.getX(), vec.getY(), vec.getZ());
    }

    public abstract Vec3i unflatten(int width, int length, int flat);

    @Debug
    public static void main(String[] args) {
        Vec3i dim = new Vec3i(5,6,7);
        Array3DOrder order = YZX;
        Vec3i.Mutable pos = new Vec3i.Mutable(0,0,0);
        int flat;
        for(int y = 0; y < dim.getY(); y++) {
            for(int z = 0; z < dim.getZ(); z++) {
                for(int x = 0; x < dim.getX(); x++) {
                    pos.set(x,y,z);
                    System.out.printf("in: %s, idx: %d, out: %s\n", pos,
                            (flat = YZX.flatten(dim.getX(), dim.getZ(), x, y, z)),
                            YZX.unflatten(dim.getX(), dim.getZ(), flat));
                }
            }
        }
    }
}
