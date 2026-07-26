package org.minerift.ether.island;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.minerift.ether.math.Vec2i;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.locks.Lock;

// TODO: for async island management, Island's may not necessarily be locked, but their
//  regions can/will be. different features may also come with different ways of accessing
//  to allow for freedom and performance without redundancy and unneeded protections;
//  more on this soon
public interface IslandGrid {

    static Synchronized synchronize(IslandGrid grid, Object mutex) {
        return new Synchronized(grid, mutex);
    }

    static Synchronized synchronize(IslandGrid grid) {
        return new Synchronized(grid);
    }

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
    default void queueForClearing(Island island) {

    }
    void queueForClearing(DeletedTile tile);
    DeletedTile getTileForClearing(Vec2i tile);
    Collection<DeletedTile> getPurgeQueue();

    int getIslandCount();

    ImmutableList<Island> getIslandsView();

    ImmutableList<Vec2i> getAvailableTiles(); // tiles that should be occupied before appending to end of grid

    /**
     * Return the next available tile in the grid.
     *
     * **NOTE:** If there is a deleted island on the grid ready to be reoccupied again,
     * this method will return the next island in the purge queue before returning
     * the next tile from the grid bounds.
     *
     * @return the next available tile in the grid to be occupied.
     */
    Vec2i getNextTile();

    IntSet getIslandIds();

    Lock readLock();
    Lock writeLock();

    class Synchronized implements IslandGrid {

        private final IslandGrid grid; // backing grid
        private final Object mutex;

        private Synchronized(IslandGrid grid, Object mutex) {
            this.grid = grid;
            this.mutex = mutex;
        }

        private Synchronized(IslandGrid grid) {
            this.grid = grid;
            this.mutex = this;
        }

        @Override
        public void registerIsland(Island island) throws IllegalStateException {
            synchronized (mutex) {
                grid.registerIsland(island);
            }
        }

        @Override
        public void unregisterIsland(int id) {
            synchronized (mutex) {
                grid.unregisterIsland(id);
            }
        }

        @Override
        public void unregisterIsland(Island island) {
            synchronized (mutex) {
                grid.unregisterIsland(island);
            }
        }

        @Override
        public Optional<Island> getIslandAt(int id) {
            synchronized (mutex) {
                return grid.getIslandAt(id);
            }
        }

        @Override
        public Optional<Island> getIslandAt(Vec2i tile) {
            synchronized (mutex) {
                return grid.getIslandAt(tile);
            }
        }

        @Override
        public boolean isTileOccupied(int id) {
            synchronized (mutex) {
                return grid.isTileOccupied(id);
            }
        }

        @Override
        public boolean isTileOccupied(Vec2i tile) {
            synchronized (mutex) {
                return grid.isTileOccupied(tile);
            }
        }

        @Override
        public boolean needsClearing(Vec2i tile) {
            synchronized (mutex) {
                return grid.needsClearing(tile);
            }
        }

        @Override
        public int getIslandCount() {
            synchronized (mutex) {
                return grid.getIslandCount();
            }
        }

        @Override
        public ImmutableList<Island> getIslandsView() {
            synchronized (mutex) {
                return grid.getIslandsView();
            }
        }

        @Override
        public ImmutableList<Vec2i> getAvailableTiles() {
            synchronized (mutex) {
                return grid.getAvailableTiles();
            }
        }

        @Override
        public Vec2i getNextTile() {
            synchronized (mutex) {
                return grid.getNextTile();
            }
        }

        @Override
        public IntSet getIslandIds() {
            synchronized (mutex) {
                return grid.getIslandIds();
            }
        }

        @Override
        public Lock readLock() {
            synchronized (mutex) {
                return grid.readLock();
            }
        }

        @Override
        public Lock writeLock() {
            synchronized (mutex) {
                return grid.writeLock();
            }
        }

        @Override
        public void queueForClearing(DeletedTile tile) {
            synchronized (mutex) {
                grid.queueForClearing(tile);
            }
        }

        @Override
        public DeletedTile getTileForClearing(Vec2i tile) {
            synchronized (mutex) {
                return grid.getTileForClearing(tile);
            }
        }

        @Override
        public Collection<DeletedTile> getPurgeQueue() {
            synchronized (mutex) {
                return grid.getPurgeQueue();
            }
        }
    }

}
