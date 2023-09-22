package org.minerift.ether.benchmark;

import org.minerift.ether.math.GridAlgorithm;
import org.minerift.ether.math.Maths;
import org.minerift.ether.math.Vec2i;
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
    public Vec2i computeTileBenchmark(ComputeTileIdState state) {
        return GridAlgorithm.computeTile(state.id);
    }

    @Benchmark
    public int computeTileIdBenchmark(ComputeTileState state) {
        return GridAlgorithm.computeTileId(Maths.strToVec2i(state.tile).getValueOrDefault(() -> null));
    }

    @Benchmark
    public int computeTileIdBenchmark_PREDETERMINED() {
        return GridAlgorithm.computeTileId(new Vec2i(-43,1235));
    }

    @Benchmark
    public Vec2i tile_parseFromStringBenchmark(ComputeTileState state) {
        return Maths.strToVec2i(state.tile).getValueOrDefault(() -> null);
    }

    /*
    private int[] getRandomNumbers(int count, int minInclusive, int maxExclusive) {
        return RANDOM.ints(count, minInclusive, maxExclusive).toArray();
    }

    private Tile[] getRandomTiles(int count, int minInclusive, int maxExclusive) {
        int[] ids = getRandomNumbers(count, minInclusive, maxExclusive);
        return Arrays.stream(ids)
                .mapToObj(GridAlgorithm::computeTile)
                .toArray(Tile[]::new);
    }
    */

}
