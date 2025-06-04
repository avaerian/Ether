package org.minerift.ether.island;

import com.google.common.collect.ImmutableList;
import org.minerift.ether.math.GridAlgorithm;
import org.minerift.ether.math.Vec2i;

import java.util.Optional;

public interface IslandGrid {

    // Registers an island onto the grid.
    // If the island id exists in the grid already and the island is deleted, the island will be replaced.
    void registerIsland(Island island);

    default void unregisterIsland(Island island) {
        unregisterIsland(island.getId());
    }

    // Removes the island from the grid completely
    void unregisterIsland(int id);

    // Returns an island at a given tile, deleted or not
    default Optional<Island> getIslandAt(Vec2i tile) {
        return getIslandAt(GridAlgorithm.computeTileId(tile), false);
    }

    default Optional<Island> getIslandAt(int id) {
        return getIslandAt(id, false);
    }

    // Returns an island at a given tile
    // If activeOnly, return the island only if active
    default Optional<Island> getIslandAt(Vec2i tile, boolean activeOnly) {
        return getIslandAt(GridAlgorithm.computeTileId(tile), activeOnly);
    }

    Optional<Island> getIslandAt(int id, boolean activeOnly);

    // Returns whether a tile has an island, deleted or not, present
    boolean isTileOccupied(Vec2i tile);

    default boolean hasActiveIslandAt(Vec2i tile) {
        return hasActiveIslandAt(GridAlgorithm.computeTileId(tile));
    }

    boolean hasActiveIslandAt(int id);

    int getIslandCount(boolean activeOnly);


    // Return view of all islands
    ImmutableList<Island> getIslandsView();

    // Get a list of islands that can be reoccupied
    ImmutableList<Island> getPurgedIslandsView();

    ImmutableList<Vec2i> getAvailableTiles();

    // Returns the next available tile that can be occupied
    Vec2i getNextTile();


}
