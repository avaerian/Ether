package org.minerift.ether.island;

import org.bukkit.Location;
import org.minerift.ether.user.EtherUser;
import org.minerift.ether.util.BukkitUtils;
import org.minerift.ether.util.math.Vec2i;

import java.util.Optional;

public class IslandManager {

    private IslandGrid grid;

    public IslandManager() {
        this.grid = new IslandGrid();
    }

    public Island createIsland(EtherUser user) {
        return IslandCreationRoutine.run(grid, user);
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

    public Optional<Island> getIslandAt(Vec2i tile) {
        return grid.getIslandAt(tile);
    }

    public Optional<Island> getIslandAt(Location location) {
        return getIslandAt(BukkitUtils.getTileAt(location));
    }
}
