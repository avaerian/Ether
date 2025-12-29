package org.minerift.ether.island;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.minerift.ether.math.Vec2i;
import org.minerift.ether.util.Note;

import java.util.Optional;
import java.util.concurrent.locks.Lock;

// TODO: for async island management, Island's may not necessarily be locked, but their
//  regions can/will be. different features may also come with different ways of accessing
//  to allow for freedom and performance without redundancy and unneeded protections;
//  more on this soon
public interface IslandGrid {

    // Registers an island onto the grid.
    // If the island id exists in the grid already and the island is deleted, the island will be replaced.
    void registerIsland(Island island) throws IllegalStateException;

    void unregisterIsland(int id);
    default void unregisterIsland(Island island) {
        unregisterIsland(island.getId());
    }

    Optional<Island> getIslandAt(int id);
    default Optional<Island> getIslandAt(Vec2i tile) {
        return getIslandAt(tile.getTileId());
    }

    boolean isTileOccupied(int id);
    default boolean isTileOccupied(Vec2i tile) {
        return isTileOccupied(tile.getTileId());
    }

    boolean needsClearing(Vec2i tile);

    int getIslandCount();

    ImmutableList<Island> getIslandsView();

    ImmutableList<Vec2i> getAvailableTiles(); // tiles that should be occupied before appending to end of grid

    @Note("Returns the next available tile that can be occupied")
    Vec2i getNextTile();

    IntSet getIslandIds();

    Lock readLock();
    Lock writeLock();


}
