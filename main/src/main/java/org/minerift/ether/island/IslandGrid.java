package org.minerift.ether.island;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.minerift.ether.GridAlgorithm;
import org.minerift.ether.util.SortedList;
import org.minerift.ether.util.math.Vec2i;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class IslandGrid {

    // All islands on the grid, including deleted islands
    // TODO: consider switching to Int2ObjectOpenHashMap for performance/safety -> use JMH to microbenchmark to see if it's worth
    private SortedList<Island> islands;

    public IslandGrid() {
        this.islands = new SortedList<>(Comparator.comparing(Island::getId));
    }

    public void registerIsland(Island island) {

        Preconditions.checkNotNull(island, "Island cannot be null when registering!");

        // If island 200 is loaded first, grid index for island 200 would be 0
        // This would remain until another island were loaded
        // If island 200 is loaded, there are 200 islands that will be loaded (list will be sorted eventually)
        // THIS IS NOT AN ISSUE FOR NOW; should be known for future reference
        validateGridContiguity();

        if(isTileOccupied(island.getTile())) {
            Island existingIsland = islands.get(island.getId());
            if(!existingIsland.isDeleted()) {
                throw new UnsupportedOperationException("Island " + island.getId() + " already exists in IslandGrid!");
            } else {
                // Overwrite island with new data
                islands.set(island.getId(), island);
                return;
            }
        }

        islands.add(island);
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
        AtomicBoolean isActive = new AtomicBoolean(false);
        getIslandAt(tile).ifPresent((island) -> isActive.set(!island.isDeleted()));
        return isActive.get();
    }

    // Get a list of islands that can be reoccupied
    public ImmutableList<Island> getPurgedIslandsView() {
        return islands.stream()
                .filter(Island::isDeleted)
                .collect(ImmutableList.toImmutableList());
    }

    public ImmutableList<Island> getIslandsView() {
        return ImmutableList.copyOf(islands);
    }

    // Returns the next available tile that can be occupied
    public Vec2i getNextTile() {
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

    /**
     * Ensures that the grid has islands, temporary or not, that
     * are contiguous. In other words, there are no gaps between indexes.
     */
    private void validateGridContiguity() {

        // One or less elements is "contiguous" (can be handled later when more elements are added)
        if(islands.size() <= 1) {
            return;
        }

        // Largest element is last, which should be size of the list when contiguous
        // If contiguous (last id is size - 1), don't handle
        int lastId = islands.get(islands.size() - 1).getId();
        if(lastId == islands.size() - 1) {
            return;
        }

        Set<Integer> excludedIds = islands.stream()
                .map(Island::getId)
                .collect(Collectors.toSet());

        IntStream.range(0, lastId)
                .filter(id -> !excludedIds.contains(id))
                .forEachOrdered(id -> {

                    Vec2i tile = GridAlgorithm.computeTile(id);
                    Island fillerIsland = Island.builder()
                            .setTile(tile, true)
                            .setDeleted(true) // tile can be overwritten
                            .build();
                    islands.add(fillerIsland);

                });
    }
}
