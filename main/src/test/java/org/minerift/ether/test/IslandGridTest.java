package org.minerift.ether.test;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.minerift.ether.GridAlgorithm;
import org.minerift.ether.island.Island;
import org.minerift.ether.island.IslandGrid;
import org.minerift.ether.util.math.Vec2i;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class IslandGridTest {

    // Test to see behavior of registering islands in random order
    @Test
    public void randomOrderIslandRegisterTest() {

        final int ISLAND_COUNT = 300;

        List<Integer> ids = getRandomlyOrderedRange(0, ISLAND_COUNT);
        IslandGrid grid = new IslandGrid();

        // Add islands to test grid
        for (int id : ids) {
            Vec2i tile = GridAlgorithm.computeTile(id);
            Island island = Island.builder()
                    .setTile(tile, true)
                    .build();

            grid.registerIsland(island);
        }

        ImmutableList<Island> islandsView = grid.getIslandsView();

        assertEquals(ISLAND_COUNT, islandsView.size());
        assertTrue(areIslandsSorted(islandsView));
    }

    @ParameterizedTest
    @MethodSource
    public void getIslandAtTest(Vec2i tile) {

        final int ISLAND_COUNT = 300;

        // Setup island grid
        IslandGrid grid = new IslandGrid();
        for(int i = 0; i < ISLAND_COUNT; i++) {
            Island island = Island.builder()
                    .setTile(grid.getNextTile(), true)
                    .build();

            grid.registerIsland(island);
        }

        // getIslandAt.get() will throw an error if null (invalid island/out-of-bounds)
        // In other words, ensure island at tile doesn't return null
        assertDoesNotThrow(() -> grid.getIslandAt(tile).get());
    }

    private static Stream<Vec2i> getIslandAtTest() {
        return Stream.of(
                GridAlgorithm.computeTile(229),
                GridAlgorithm.computeTile(23),
                GridAlgorithm.computeTile(68),
                GridAlgorithm.computeTile(12)
        );
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
