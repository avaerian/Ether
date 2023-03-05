package org.minerift.ether.island;

import org.bukkit.Location;
import org.minerift.ether.GridAlgorithm;

public class Island {

    private Location topLeftBound;
    private Location bottomRightBound;

    // These 2 pieces of data can be calculated from each other
    private int id;
    private Tile tile;

    private boolean isDeleted;

    // Private constructor
    private Island(Island.Builder builder) {
        // TODO: load all values from builder to object
        this.tile = builder.tile;
        this.id = builder.id;
        this.isDeleted = builder.isDeleted;
    }

    public static Island.Builder builder() {
        return new Island.Builder();
    }

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
        private boolean isDeleted = false;

        /**
         *
         * @param tile
         * @param withId Whether the builder should also set the id from tile
         * @return
         */
        public Builder setTile(Tile tile, boolean withId) {
            this.tile = tile;
            if(withId) this.id = tile.getId();
            return this;
        }

        public Builder setDeleted(boolean isDeleted) {
            this.isDeleted = isDeleted;
            return this;
        }

        public Island build() {
            validate();
            return new Island(this);
        }

        // TODO: implement
        private void validate() {
            // Tile and Id are required
        }

    }

}
