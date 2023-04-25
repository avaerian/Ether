package org.minerift.ether.benchmark;

import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Deprecated
public class Benchmarks {

    private final static Random RANDOM;

    static {
        RANDOM = new Random();
    }

    public static void main(String[] args) throws RunnerException {

        Options options = new OptionsBuilder()
                //.include(PermissionSetBenchmarks.class.getSimpleName())
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

    // TODO: refactor benchmarks into separate classes
    /*public static class PermissionSetBenchmarks {

        public static class BenchmarkState {
            PermissionSet set;

            @Setup
            public void setup() {
                this.set = new PermissionSet();
                set.setPermissions(IslandRole.OWNER, EnumSet.allOf(IslandPermission.class));
                set.setPermissions(IslandRole.MEMBER, EnumSet.range(IslandPermission.BLOCK_BREAK, IslandPermission.ENTITY_INTERACT));
                set.setPermissions(IslandRole.VISITOR, EnumSet.noneOf(IslandPermission.class));
            }
        }

        @Benchmark
        public boolean hasPermissionBenchmark() {
            return set.hasPermission(IslandRole.MEMBER, IslandPermission.ENTITY_DAMAGE); // not included in range; expect false
        }

    }*/




    /*private int[] getRandomNumbers(int count, int minInclusive, int maxExclusive) {
        return RANDOM.ints(count, minInclusive, maxExclusive).toArray();
    }

    private Tile[] getRandomTiles(int count, int minInclusive, int maxExclusive) {
        int[] ids = getRandomNumbers(count, minInclusive, maxExclusive);
        return Arrays.stream(ids)
                .mapToObj(GridAlgorithm::computeTile)
                .toArray(Tile[]::new);
    }*/

}
