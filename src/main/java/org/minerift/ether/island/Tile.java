package org.minerift.ether.island;

// Immutable by default
// Use Tile.Mutable for math-related stuffs
public class Tile {

    public final static Tile ZERO = new Tile(0, 0);

    private int x, z;

    public Tile(int x, int z) {
        this.x = x;
        this.z = z;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public static class Mutable extends Tile {

        public Mutable(int x, int z) {
            super(x, z);
        }

        public void setX(int x) {
            super.x = x;
        }

        public void setZ(int z) {
            super.z = z;
        }

        public void add(int x, int z) {
            super.x += x;
            super.z += z;
        }

        public void subtract(int x, int z) {
            super.x -= x;
            super.z -= z;
        }
    }
}