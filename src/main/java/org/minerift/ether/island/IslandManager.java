package org.minerift.ether.island;

import org.bukkit.Location;
import org.minerift.ether.GridAlgorithm;
import org.minerift.ether.utils.BukkitUtils;

import java.util.Optional;

public class IslandManager {

    private IslandGrid grid;

    public IslandManager() {
        this.grid = new IslandGrid();
    }

    public void createIsland() {

        // Create island instance
        // TODO: island builder
        Island island = null;

        // Set island tile to next available tile on grid
        Tile tile = grid.getNextTile();

        // Paste schematic/structure onto tile
        // Register island
        grid.registerIsland(island);
        // Teleport player (could be a callback)
    }

    public void deleteIsland(Island island) {

        // Mark island as deleted
        island.markDeleted();

        // Clear all island information
        // Scan island and clear/set to air
        // Remove island references from players on island team
    }

    public Optional<Island> getIslandAt(Tile tile) {
        return grid.getIslandAt(tile);
    }

    public Optional<Island> getIslandAt(Location location) {
        return getIslandAt(BukkitUtils.getTileAt(location));
    }

    /*public GridAlgorithm getGridAlgorithm() {
        return grid.getGridAlgorithm();
    }*/
}
