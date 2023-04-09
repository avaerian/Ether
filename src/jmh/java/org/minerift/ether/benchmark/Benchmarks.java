package org.minerift.ether.benchmark;

import org.minerift.ether.GridAlgorithm;
import org.minerift.ether.island.Tile;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Benchmarks {

    private final static Random RANDOM;

    static {
        RANDOM = new Random();
    }

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

    public static void main(String[] args) throws RunnerException {

        Options options = new OptionsBuilder()
                .include(Benchmarks.class.getSimpleName())
                .mode(Mode.AverageTime)
                //.warmupTime(TimeValue.seconds(1))
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

    private int[] getRandomNumbers(int count, int minInclusive, int maxExclusive) {
        return RANDOM.ints(count, minInclusive, maxExclusive).toArray();
    }

    private Tile[] getRandomTiles(int count, int minInclusive, int maxExclusive) {
        int[] ids = getRandomNumbers(count, minInclusive, maxExclusive);
        return Arrays.stream(ids)
                .mapToObj(GridAlgorithm::computeTile)
                .toArray(Tile[]::new);
    }

    @Benchmark
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
    }

}
