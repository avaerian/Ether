package org.minerift.ether.benchmark;

import org.minerift.ether.island.IslandPermission;
import org.minerift.ether.island.IslandRole;
import org.minerift.ether.island.PermissionSet;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

public class PermissionSetBenchmarks {

    public static void main(String[] args) throws RunnerException {

        Options options = new OptionsBuilder()
                .include(PermissionSetBenchmarks.class.getSimpleName())
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
    public boolean hasPermissionBenchmark(BenchmarkState state) {
        return state.set.hasPermission(IslandRole.MEMBER, IslandPermission.ENTITY_DAMAGE); // not included in range; expect false
    }
}
