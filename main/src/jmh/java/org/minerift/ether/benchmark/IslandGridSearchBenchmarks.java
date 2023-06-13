package org.minerift.ether.benchmark;

import org.minerift.ether.GridAlgorithm;
import org.minerift.ether.island.*;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class IslandGridSearchBenchmarks {

    private static final int TILE_COUNT = 365875;

    // Last tile is selected as a possible worst-case scenario (think iterating an array)
    private static final int TILE_ID_TO_FIND = 200384;
    private static final Tile TILE_TO_FIND = GridAlgorithm.computeTile(TILE_ID_TO_FIND);

    public static void main(String[] args) throws RunnerException {

        Options options = new OptionsBuilder()
                .include(IslandGridSearchBenchmarks.class.getSimpleName())
                .mode(Mode.AverageTime)
                .timeUnit(TimeUnit.NANOSECONDS)
                .warmupIterations(3)
                .warmupTime(TimeValue.seconds(5))
                .threads(1)
                .measurementIterations(6)
                .measurementTime(TimeValue.seconds(2))
                .forks(1)
                .shouldFailOnError(true)
                .shouldDoGC(true)
                .build();

        new Runner(options).run();
    }

    @State(Scope.Benchmark)
    public static class ArrayListImplState {
        List<Island> islands;
        Island islandToFind; // for binary search

        @Setup
        public void setup() {
            this.islands = new ArrayList<>(TILE_COUNT);
            for(int i = 0; i < TILE_COUNT; i++) {
                Island island = Island.builder()
                        .setTile(GridAlgorithm.computeTile(i), true)
                        .setDeleted(false)
                        .definePermissions(IslandRole.VISITOR)
                        .build();
                islands.add(island);
            }

            this.islandToFind = Island.builder()
                    .setTile(TILE_TO_FIND, true)
                    .setDeleted(false)
                    .build();
        }
    }

    @State(Scope.Benchmark)
    public static class HashMapImplState {

        Map<Tile, Island> islands;

        @Setup
        public void setup() {
            this.islands = new HashMap<>();
            for(int i = 0; i < TILE_COUNT; i++) {
                Tile tile = GridAlgorithm.computeTile(i);
                Island island = Island.builder()
                        .setTile(tile, true)
                        .setDeleted(false)
                        .definePermissions(IslandRole.VISITOR)
                        .build();
                islands.put(tile, island);
            }
        }

    }

    @State(Scope.Benchmark)
    public static class IslandGridState {
        IslandGrid grid;

        @Setup
        public void setup() {
            this.grid = new IslandGrid();
            for(int i = 0; i < TILE_COUNT; i++) {
                Island island = Island.builder()
                        .setTile(grid.getNextTile(), true)
                        .setDeleted(false)
                        .definePermissions(IslandRole.VISITOR)
                        .build();
                grid.registerIsland(island);
            }
        }
    }

    @Benchmark
    public Optional<Island> arrayListLinear_findIslandBenchmark(ArrayListImplState state) {
        for(Island island : state.islands) {
            if(island.getTile().equals(TILE_TO_FIND)) {
                return Optional.of(island);
            }
        }
        return Optional.empty();
    }

    @Benchmark
    public Optional<Island> arrayListBinary_findIslandBenchmark(ArrayListImplState state) {
        int idx = Collections.binarySearch(state.islands, state.islandToFind, Comparator.comparing(Island::getId));
        return Optional.of(state.islands.get(idx));
    }

    @Benchmark
    public Optional<Island> islandGrid_findIslandBenchmark(IslandGridState state) {
        return state.grid.getIslandAt(TILE_TO_FIND);
    }

    @Benchmark
    public Optional<Island> hashMap_findIslandBenchmark(HashMapImplState state) {
        return Optional.ofNullable(state.islands.get(TILE_TO_FIND));
    }
}
