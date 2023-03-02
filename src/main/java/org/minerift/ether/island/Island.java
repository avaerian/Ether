package org.minerift.ether.island;

import org.bukkit.Location;

public class Island {

    private Location topLeftBound;
    private Location bottomRightBound;

    // These 2 pieces of data can be calculated from each other
    private int id;
    private Tile tile;

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

    public void setDeleted() {
        this.isDeleted = true;
    }

}
