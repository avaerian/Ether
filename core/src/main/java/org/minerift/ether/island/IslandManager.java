package org.minerift.ether.island;

import org.bukkit.Location;
import org.minerift.ether.Ether;
import org.minerift.ether.math.Vec2i;
import org.minerift.ether.user.EtherUser;
import org.minerift.ether.util.BukkitUtils;

import java.util.*;

public class IslandManager {

    private IslandGrid grid;

    public IslandManager() {
        this(new DefaultIslandGrid());
    }

    public IslandManager(IslandGrid grid) {
        this.grid = grid;
    }

    public Set<Integer> getKeySet() {
        var data = ((DefaultIslandGrid)grid).getData(); // be weary of this
        Set<Integer> keys = new HashSet<>(data.size());
        for(Island island : data) {
            // Return only active island ids for key set
            if(island != null && !island.isDeleted()) {
                keys.add(island.getId());
            }
        }
        return keys;
    }

    // Get multiple islands
    // Returns active, deleted, and null islands
    public List<Island> getIslands(Collection<Integer> ids) {
        List<Island> islands = ids.isEmpty() ? Collections.emptyList() : new ArrayList<>(ids.size());
        for(int id : ids) {
            islands.add(grid.getIslandAt(id).orElse(null));
        }
        return islands;
    }

    public Island createIsland(EtherUser user) {
        return IslandCreationRoutine.run(grid, user);
    }

    public void deleteIsland(Island island) { // TODO

        // Mark island as deleted
        island.markDeleted();

        // Clear all island information (?)

        // Remove all entities in world within island region
        // Scan island and clear/set to air
        // Remove island references from players on island team
        island.getTeamMembers().forEach(island::removeTeamMember);
        // Teleport players back to spawn
    }

    public Optional<Island> getIslandAt(Vec2i tile) {
        return grid.getIslandAt(tile);
    }

    public Optional<Island> getIslandAt(Integer islandId) {
        return grid.getIslandAt(islandId);
    }

    public Optional<Island> getIslandAt(Location location) {
        return getIslandAt(BukkitUtils.getTileAt(location));
    }
}
