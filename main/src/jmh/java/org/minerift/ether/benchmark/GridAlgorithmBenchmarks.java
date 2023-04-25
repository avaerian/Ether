package org.minerift.ether.benchmark;

import org.minerift.ether.GridAlgorithm;
import org.minerift.ether.island.*;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class GridAlgorithmBenchmarks {

    @State(Scope.Benchmark)
    public static class BenchmarkState {

        @Param({
                //"32645",
                "12789",
                "44322",
                //"21897",
                "3846",
                //"43567"
        })
        public int id;

        @Param({
                //"(-25, 90)",
                "(-37, -57)",
                "(-93, 105)",
                //"(74, -4)",
                "(-31, 1)",
                //"(-9, 104)"
        })
        public String tile;

    }

    @State(Scope.Benchmark)
    public static class AltImplementationState {
        final int TILE_COUNT = 5000;
        final int TILE_TO_FIND = TILE_COUNT - 1; // last element in list
        List<Tile> tiles;

        @Setup
        public void setup() {
            this.tiles = new ArrayList<>(TILE_COUNT);
            for(int i = 0; i < TILE_COUNT; i++) {
                tiles.add(GridAlgorithm.computeTile(i));
            }
        }
    }

    @State(Scope.Benchmark)
    public static class IslandGridState {
        final int  TILE_COUNT = 5000;
        final int  TILE_ID_TO_FIND = TILE_COUNT - 1; // last element in list
        final Tile TILE_TO_FIND = GridAlgorithm.computeTile(TILE_ID_TO_FIND);
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

    public static void main(String[] args) throws RunnerException {

        Options options = new OptionsBuilder()
                .include(GridAlgorithmBenchmarks.class.getSimpleName())
                .mode(Mode.AverageTime)
                .timeUnit(TimeUnit.NANOSECONDS)
                .warmupIterations(3)
                .warmupTime(TimeValue.seconds(5))
                .threads(1)
                .measurementIterations(3)
                .measurementTime(TimeValue.seconds(2))
                .forks(1)
                .shouldFailOnError(true)
                .shouldDoGC(true)
                .build();

        new Runner(options).run();
    }

    /*@Benchmark
    public Tile gridAlgorithm_computeTileBenchmark(BenchmarkState state) {
        return GridAlgorithm.computeTile(state.id);
    }

    @Benchmark
    public int gridAlgorithm_computeTileIdBenchmark(BenchmarkState state) {
        return GridAlgorithm.computeTileId(new Tile(state.tile));
    }

    @Benchmark
    public int gridAlgorithm_computeTileIdBenchmark_PREDETERMINED() {
        return GridAlgorithm.computeTileId(new Tile(-43,1235));
    }

    @Benchmark
    public Tile tile_parseFromStringBenchmark(BenchmarkState state) {
        return new Tile(state.tile);
    }*/

    @Benchmark
    public Tile gridAlgorithm_altImplementation_findTileBenchmark(AltImplementationState state) {
        for(Tile tile : state.tiles) {
            if(tile.getId() == state.TILE_TO_FIND) {
                return tile;
            }
        }
        return null;
    }

    @Benchmark
    public Optional<Island> gridAlgorithm_myIslandGrid_findTileBenchmark(IslandGridState state) {
        return state.grid.getIslandAt(state.TILE_TO_FIND);
    }

}
