package org.minerift.ether.island;

import org.minerift.ether.GridAlgorithm;

import java.util.Objects;

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

    public int getId() {
        return GridAlgorithm.computeTileId(this);
    }

    public int distanceTo(Tile tile) {
        int distX = (x - tile.getX()) * (x - tile.getX());
        int distZ = (z - tile.getZ()) * (z - tile.getZ());
        return (int) Math.sqrt(distX + distZ);
    }

    public Tile.Mutable asMutable() {
        return (this instanceof Tile.Mutable) ? (Tile.Mutable) this : new Tile.Mutable(x, z);
        //return new Tile.Mutable(x, z);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tile tile = (Tile) o;
        return x == tile.x && z == tile.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, z);
    }

    @Override
    public String toString() {
        return "(" + x + ", " + z + ")";
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