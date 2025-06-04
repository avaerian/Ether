package org.minerift.ether.island;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.minerift.ether.Ether;
import org.minerift.ether.math.GridAlgorithm;
import org.minerift.ether.math.Vec2i;
import org.minerift.ether.util.collect.IndexedList;

import java.util.Optional;
import java.util.logging.Level;

public class DefaultIslandGrid implements IslandGrid {

    // All islands on the grid, including deleted islands
    private final IndexedList<Island> islands;

    public DefaultIslandGrid() {
        this.islands = new IndexedList<>(Island::getId, Island::isDeleted);
    }

    public DefaultIslandGrid(int initialSize) {
        this.islands = new IndexedList<>(initialSize, Island::getId, Island::isDeleted);
    }

    @Override
    public void registerIsland(Island island) {
        Preconditions.checkNotNull(island, "Cannot register null island!");
        try {
            islands.add(island);
        } catch (UnsupportedOperationException ex) {
            Ether.getLogger().log(Level.SEVERE, "Island " + island.getId() + " already exists in IslandGrid!", ex);
        }
    }

    @Override
    public void unregisterIsland(int id) {
        islands.set(id, null);
    }

    // Returns an island from index
    // If activeOnly, return the island only if active (not deleted)
    @Override
    public Optional<Island> getIslandAt(int idx, boolean activeOnly) {
        Island island = idx < islands.size() ? islands.get(idx) : null;
        if(island != null && activeOnly && island.isDeleted()) {
            island = null;
        }
        return Optional.ofNullable(island);
    }

    @Override
    public boolean isTileOccupied(Vec2i tile) {
        return getIslandAt(tile).isPresent();
    }

    @Override
    public boolean hasActiveIslandAt(int id) {
        final Optional<Island> island = getIslandAt(id);
        return island.isPresent() && !island.get().isDeleted();
    }

    @Override
    public int getIslandCount(boolean activeOnly) {
        int sum = 0;
        for(int i = 0; i < islands.size(); i++) {
            Island island = islands.get(i);
            // if activeOnly and the island is not null or deleted, add 1
            // if not activeOnly and island is not null, add 1
            if(island != null) {
                if(activeOnly && island.isDeleted()) {
                    continue;
                }
                sum++;
            }
        }
        return sum;
    }

    @Override
    public ImmutableList<Island> getPurgedIslandsView() {
        return islands.stream()
                .filter((island) -> island != null && island.isDeleted())
                .collect(ImmutableList.toImmutableList());
    }

    @Override
    public ImmutableList<Vec2i> getAvailableTiles() {
        ImmutableList.Builder<Vec2i> builder = ImmutableList.builderWithExpectedSize(islands.size());
        for(int i = 0; i < islands.size(); i++) {
            Island island = islands.get(i);
            if(island == null || island.isDeleted()) {
                builder.add(GridAlgorithm.computeTile(i));
            }
        }
        return builder.build();
    }

    @Override
    public ImmutableList<Island> getIslandsView() {
        return islands.getImmutableView();
    }

    @Override
    public Vec2i getNextTile() {
        for(int i = 0; i < islands.size(); i++) {
            Island island = islands.get(i);
            if(island == null || island.isDeleted()) {
                return GridAlgorithm.computeTile(i);
            }
        }
        return getNextTileFromGridBounds();
    }

    // Alternative implementation
    public Vec2i getNextTileAlternate() {
        ImmutableList<Vec2i> tiles = getAvailableTiles();
        return !tiles.isEmpty() ? tiles.get(0) : getNextTileFromGridBounds();
    }

    public IndexedList<Island> getData() {
        return islands;
    }

    // Returns the next island/tile id at the end of the grid.
    // Does not consider purged islands.
    // This exists in case the logic needs to change for any reason.
    // EXAMPLE: If the last island has an id of 37, size will return 38.
    private int getNextIdFromGridBounds() {
        return islands.size();
    }

    // Returns the next tile at the end of the grid
    private Vec2i getNextTileFromGridBounds() {
        return GridAlgorithm.computeTile(getNextIdFromGridBounds());
    }
}
