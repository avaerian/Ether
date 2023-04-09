package org.minerift.ether.island;

import org.bukkit.Location;
import org.minerift.ether.GridAlgorithm;
import org.minerift.ether.user.EtherUser;
import org.minerift.ether.utils.BukkitUtils;

import java.util.Optional;

public class IslandManager {

    private IslandGrid grid;

    public IslandManager() {
        this.grid = new IslandGrid();
    }

    public void createIsland() {

        // Find next available tile on grid
        Tile tile = grid.getNextTile();

        // Create island instance
        // TODO: implement builder for Island
        Island island = Island.builder()
                .setTile(tile, true)
                .build();

        // Register island
        grid.registerIsland(island);

        // Paste schematic/structure onto tile
        // Teleport player (could be a callback)
    }

    public void deleteIsland(Island island) {

        // Mark island as deleted
        island.markDeleted();

        // Clear all island information
        // Scan island and clear/set to air
        // Remove island references from players on island team
        island.getTeamMembers().forEach(member -> member.setIsland(null));
    }

    public Optional<Island> getIslandAt(Tile tile) {
        return grid.getIslandAt(tile);
    }

    public Optional<Island> getIslandAt(Location location) {
        return getIslandAt(BukkitUtils.getTileAt(location));
    }
}
