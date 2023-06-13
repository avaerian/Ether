package org.minerift.ether.benchmark;

import org.minerift.ether.GridAlgorithm;
import org.minerift.ether.island.Tile;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.util.concurrent.TimeUnit;

public class GridAlgorithmBenchmarks {

    public static void main(String[] args) throws RunnerException {

        Options options = new OptionsBuilder()
                .include(GridAlgorithmBenchmarks.class.getSimpleName())
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
    public static class ComputeTileState {

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
    public static class ComputeTileIdState {
        @Param({
                //"32645",
                "12789",
                "44322",
                //"21897",
                "3846",
                //"43567"
        })
        public int id;
    }

    @Benchmark
    public Tile computeTileBenchmark(ComputeTileIdState state) {
        return GridAlgorithm.computeTile(state.id);
    }

    @Benchmark
    public int computeTileIdBenchmark(ComputeTileState state) {
        return GridAlgorithm.computeTileId(new Tile(state.tile));
    }

    @Benchmark
    public int computeTileIdBenchmark_PREDETERMINED() {
        return GridAlgorithm.computeTileId(new Tile(-43,1235));
    }

    @Benchmark
    public Tile tile_parseFromStringBenchmark(ComputeTileState state) {
        return new Tile(state.tile);
    }

}
