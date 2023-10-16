package org.minerift.ether.island;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.minerift.ether.Ether;
import org.minerift.ether.math.GridAlgorithm;
import org.minerift.ether.math.Vec2i;
import org.minerift.ether.util.IndexedBuffer;

import java.util.Optional;
import java.util.logging.Level;

public class IslandGridV2 {

    // All islands on the grid, including deleted islands
    private IndexedBuffer<Island> islands;

    public IslandGridV2() {
        this.islands = new IndexedBuffer<>(Island::getId, Island::isDeleted);
    }

    public IslandGridV2(int initialSize) {
        this.islands = new IndexedBuffer<>(initialSize, Island::getId, Island::isDeleted);
    }

    public void registerIsland(Island island) {

        Preconditions.checkNotNull(island, "Island cannot be null when registering!");

        try {
            islands.add(island);
        } catch (UnsupportedOperationException ex) {
            Ether.getLogger().log(Level.SEVERE, "Island " + island.getId() + " already exists in IslandGrid!", ex);
        }
    }

    // Returns an island at a given tile, deleted or not
    public Optional<Island> getIslandAt(Vec2i tile) {
        return getIslandAt(tile, false);
    }

    // Returns an island at a given tile.
    // If activeOnly, return the island only if active.
    public Optional<Island> getIslandAt(Vec2i tile, boolean activeOnly) {

        // Get island at tile location
        final int id = GridAlgorithm.computeTileId(tile);
        Island island = id >= islands.size() ? null : islands.get(id);

        // Handle activeOnly
        if(activeOnly) {
            island = island != null && !island.isDeleted() ? island : null;
        }
        return Optional.ofNullable(island);
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
                .filter(Island::isDeleted)
                .collect(ImmutableList.toImmutableList());
    }

    public ImmutableList<Island> getIslandsView() {
        return islands.getImmutableView();
    }

    // Returns the next available tile that can be occupied
    public Vec2i getNextTile() {
        // TODO: handle null islands in buffer?
        ImmutableList<Island> purgedIslands = getPurgedIslandsView();
        return purgedIslands.isEmpty()
                ? getNextTileFromGridBounds()
                : purgedIslands.get(0).getTile();
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
