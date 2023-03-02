package org.minerift.ether.island;

import com.google.common.collect.ImmutableList;
import org.minerift.ether.GridAlgorithm;
import org.minerift.ether.utils.SortedList;

import java.util.Optional;

public class IslandGrid {

    // Active islands on the grid
    private SortedList<Island> islands;

    // Algorithm for calculating tile coordinates and tile IDs
    private GridAlgorithm algorithm;

    public IslandGrid() {
        this.algorithm = new GridAlgorithm();
    }

    public void registerIsland() {
        
    }

    public void deleteIsland(Island island) {

    }

    public Optional<Island> getIslandAt(Tile tile) {
        final int id = algorithm.computeTileId(tile);
        Island island = id >= islands.size() ? null : islands.get(id);
        return Optional.ofNullable(island);
    }

    public boolean isTileOccupied(Tile tile) {
        return getIslandAt(tile).isPresent();
    }

    // Get a list of islands that can be reoccupied
    public ImmutableList<Island> getPurgedIslands() {
        return islands.stream()
                .filter(Island::isDeleted)
                .collect(ImmutableList.toImmutableList());
    }

    public ImmutableList<Island> getIslandsView() {
        return ImmutableList.copyOf(islands);
    }

    // Returns the next island/tile id at the end of the grid
    // Does not consider purged islands
    private int getNextIdFromGridBounds() {
        // EXAMPLE: If the last island has an id of 37, size will return 38
        return islands.size();
    }

    // Returns the next tile at the end of the grid
    private Tile getNextTileFromGridBounds() {
        return algorithm.computeTile(getNextIdFromGridBounds());
    }

    // Returns the next tile that can be occupied
    private Tile getNextTile() {
        ImmutableList<Island> purgedIslands = getPurgedIslands();
        return purgedIslands.isEmpty()
                ? getNextTileFromGridBounds()
                : purgedIslands.get(0).getTile();
    }
}
