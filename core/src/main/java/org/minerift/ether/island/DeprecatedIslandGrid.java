package org.minerift.ether.island;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.minerift.ether.math.GridAlgorithm;
import org.minerift.ether.math.Vec2i;
import org.minerift.ether.util.SortedList;

import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Deprecated
public class DeprecatedIslandGrid implements IslandGrid {

    // All islands on the grid, including deleted islands
    private SortedList<Island> islands;

    public DeprecatedIslandGrid() {
        this.islands = new SortedList<>(Comparator.comparing(Island::getId));
    }

    @Override
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

    @Override
    public void unregisterIsland(int id) {
        throw new UnsupportedOperationException("Unimplemented");
    }

    @Override
    public Optional<Island> getIslandAt(int id, boolean activeOnly) {
        Island island = id >= islands.size() ? null : islands.get(id);

        // Handle activeOnly
        if(activeOnly) {
            island = island != null && !island.isDeleted() ? island : null;
        }
        return Optional.ofNullable(island);
    }

    // Returns whether a tile has an island, deleted or not, present
    @Override
    public boolean isTileOccupied(Vec2i tile) {
        return getIslandAt(tile).isPresent();
    }

    @Override
    public ImmutableList<Island> getIslandsView() {
        return ImmutableList.copyOf(islands);
    }

    @Override
    public boolean hasActiveIslandAt(int id) {
        Optional<Island> island = getIslandAt(id);
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

    public SortedList<Island> getData() {
        return islands;
    }

    @Override
    public ImmutableList<Island> getPurgedIslandsView() {
        return islands.stream()
                .filter(Island::isDeleted)
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
