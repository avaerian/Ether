package org.minerift.ether.island;

import org.bukkit.Location;
import org.minerift.ether.config.MainConfiguration;

import java.util.Optional;

public class IslandManager {

    private IslandGrid grid;

    public IslandManager() {
        this.grid = new IslandGrid();
    }

    public void createIsland() {

        // Create island instance
        // Locate next available tile on grid
        // Occupy available grid tile
        // Paste schematic/structure onto tile
        // Register island
        // Teleport player (could be a callback)
    }

    public void deleteIsland() {
        // TBD
    }

    public Optional<Island> getIslandAt(Tile tile) {
        return grid.getIslandAt(tile);
    }

    public Optional<Island> getIslandAt(Location location) {
        return getIslandAt(getTileAt(location));
    }

    // TODO: move this to another class for decoupling?
    private Tile getTileAt(Location location) {
        final int TILE_SIZE = MainConfiguration.TILE_SIZE;
        int tileX = (int) Math.floor(location.getX() / TILE_SIZE);
        int tileZ = (int) Math.floor(location.getZ() / TILE_SIZE);
        return new Tile(tileX, tileZ);
    }

}
