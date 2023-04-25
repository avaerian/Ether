package org.minerift.ether.island;

import org.bukkit.Location;
import org.minerift.ether.user.EtherUser;
import org.minerift.ether.utils.BukkitUtils;

import java.util.Optional;

public class IslandManager {

    private IslandGrid grid;

    public IslandManager() {
        this.grid = new IslandGrid();
    }

    public Island createIsland(EtherUser user) {

        // Find next available tile on grid
        Tile tile = grid.getNextTile();

        // Create island instance
        // TODO: implement builder for Island
        Island island = Island.builder()
                .setTile(tile, true)
                .setDeleted(false)
                .addTeamMember(user, IslandRole.OWNER)
                .build();

        // Register island
        grid.registerIsland(island);

        // Paste schematic/structure onto tile
        // Teleport player


        return island;
    }

    public void deleteIsland(Island island) {

        // Mark island as deleted
        island.markDeleted();

        // Clear all island information
        // Remove all entities in world within island region
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
