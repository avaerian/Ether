package org.minerift.ether.island;

import org.bukkit.Location;

public class Island {

    private Location topLeftBound;
    private Location bottomRightBound;

    // These 2 pieces of data can be calculated from each other
    private int id;
    private Tile tile;

    // Private constructor
    private Island(Island.Builder builder) {
        // TODO: load all values from builder to object
    }

    public static Island.Builder builder() {
        return new Island.Builder();
    }

    private boolean isDeleted;

    public int getId() {
        return id;
    }

    public Tile getTile() {
        return tile;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void markDeleted() {
        this.isDeleted = true;
    }

    public static class Builder {

        private Tile tile;
        private int id;

        public void setTile(Tile tile) {

        }

        public Island build() {
            validate();
            return new Island(this);
        }

        private void validate() {

        }

    }

}
