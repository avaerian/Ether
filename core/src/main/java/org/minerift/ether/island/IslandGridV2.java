package org.minerift.ether.island;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.minerift.ether.Ether;
import org.minerift.ether.math.GridAlgorithm;
import org.minerift.ether.math.Vec2i;
import org.minerift.ether.util.IndexedList;

import java.util.Optional;
import java.util.logging.Level;

public class IslandGridV2 {

    // All islands on the grid, including deleted islands
    private final IndexedList<Island> islands;

    public IslandGridV2() {
        this.islands = new IndexedList<>(Island::getId, Island::isDeleted);
    }

    public IslandGridV2(int initialSize) {
        this.islands = new IndexedList<>(initialSize, Island::getId, Island::isDeleted);
    }

    public void registerIsland(Island island) {

        Preconditions.checkNotNull(island, "Cannot register null island!");

        try {
            islands.add(island);
        } catch (UnsupportedOperationException ex) {
            Ether.getLogger().log(Level.SEVERE, "Island " + island.getId() + " already exists in IslandGrid!", ex);
        }
    }


    // Returns an island from index, deleted or not
    public Optional<Island> getIslandAt(int idx) {
        return getIslandAt(idx, false);
    }

    // Returns an island from index
    // If activeOnly, return the island only if active (not deleted)
    public Optional<Island> getIslandAt(int idx, boolean activeOnly) {
        Island island = idx < islands.size() ? islands.get(idx) : null;
        if(island != null && activeOnly && island.isDeleted()) {
            island = null;
        }
        return Optional.ofNullable(island);
    }

    // Returns an island at a given tile, deleted or not
    public Optional<Island> getIslandAt(Vec2i tile) {
        return getIslandAt(GridAlgorithm.computeTileId(tile), false);
    }

    // Returns an island at a given tile
    // If activeOnly, return the island only if active
    public Optional<Island> getIslandAt(Vec2i tile, boolean activeOnly) {
        return getIslandAt(GridAlgorithm.computeTileId(tile), activeOnly);
    }

    // Returns whether a tile has an island, deleted or not, present
    public boolean isTileOccupied(Vec2i tile) {
        return getIslandAt(tile).isPresent();
    }

    // Returns whether a tile has an active island (not deleted)
    public boolean hasActiveIslandAtTile(Vec2i tile) {
        final Optional<Island> island = getIslandAt(tile);
        return island.isPresent() && !island.get().isDeleted();
    }

    // Get a list of islands that can be reoccupied
    public ImmutableList<Island> getPurgedIslandsView() {
        return islands.stream()
                .filter((island) -> island != null && island.isDeleted())
                .collect(ImmutableList.toImmutableList());
    }

    public ImmutableList<Vec2i> getAvailableTiles() {
        ImmutableList.Builder<Vec2i> builder = ImmutableList.builder();
        //ImmutableList.Builder<Vec2i> builder = ImmutableList.builderWithExpectedSize(islands.size());
        for(int i = 0; i < islands.size(); i++) {
            Island island = islands.get(i);
            if(island == null || island.isDeleted()) {
                builder.add(GridAlgorithm.computeTile(i));
            }
        }
        return builder.build();
    }

    public ImmutableList<Island> getIslandsView() {
        return islands.getImmutableView();
    }

    // Returns the next available tile that can be occupied
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

    // TODO: create immutable copy if possible (?)
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
