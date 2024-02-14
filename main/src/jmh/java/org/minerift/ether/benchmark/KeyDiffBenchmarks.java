package org.minerift.ether.benchmark;

import org.minerift.ether.database.sql.KeyDiff;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class KeyDiffBenchmarks {

    private final static Random RANDOM = new Random();

    public static void main(String[] args) throws RunnerException {

        Options options = new OptionsBuilder()
                .include(KeyDiffBenchmarks.class.getSimpleName())
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

    @State(Scope.Group)
    public static class KeyDiffState {
        Set<Integer> oldSet;
        Set<Integer> newSet;

        @Setup
        public void setup() {
            this.oldSet = randomNumSet(100, 0, 1000);
            this.newSet = randomNumSet(100, 0, 1000);
        }
    }

    private static Set<Integer> randomNumSet(int count, int minInclusive, int maxExclusive) {
        return RANDOM.ints(count, minInclusive, maxExclusive).boxed().collect(Collectors.toSet());
    }

    @Group
    @Benchmark
    public KeyDiff.Diff<Integer>[] getDiffsBitsBenchmark(KeyDiffState state) {
        return KeyDiff.getDiffs(state.oldSet, state.newSet);
    }

    @Group
    @Benchmark
    public KeyDiff.Diff<Integer>[] getDiffsIfsBenchmark(KeyDiffState state) {
        return KeyDiff.getDiffsAlternate(state.oldSet, state.newSet);
    }

    @Group
    @Benchmark
    public Map<KeyDiff.DiffType, List<Integer>> partitionDiffsBenchmark(KeyDiffState state) {
        return KeyDiff.partitionDiffs(state.oldSet, state.newSet);
    }

}
