package org.minerift.ether.test;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import org.minerift.ether.GridAlgorithm;
import org.minerift.ether.island.Island;
import org.minerift.ether.island.IslandGrid;
import org.minerift.ether.island.Tile;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IslandGridTest {

    // Test to see behavior of registering islands in random order
    @Test
    public void randomOrderIslandRegisterTest() {

        final int ISLAND_COUNT = 300;

        List<Integer> ids = getRandomlyOrderedRange(0, ISLAND_COUNT);
        IslandGrid grid = new IslandGrid();

        // Add islands to test grid
        for (int id : ids) {
            Tile tile = GridAlgorithm.computeTile(id);
            Island island = Island.builder()
                    .setTile(tile, true)
                    .build();

            grid.registerIsland(island);
        }

        ImmutableList<Island> islandsView = grid.getIslandsView();

        assertEquals(ISLAND_COUNT, islandsView.size());
        assertTrue(areIslandsSorted(islandsView));
    }

    private List<Integer> getRandomlyOrderedRange(int minInclusive, int maxExclusive) {
        List<Integer> range = IntStream.range(minInclusive, maxExclusive)
                .boxed()
                .collect(Collectors.toList());
        Collections.shuffle(range);
        return range;
    }

    private boolean areIslandsSorted(List<Island> islands) {
        Island[] initial = islands.toArray(Island[]::new);
        Island[] sorted = islands.stream()
                .sorted(Comparator.comparing(Island::getId))
                .toArray(Island[]::new);
        return Arrays.equals(initial, sorted);
    }

}
