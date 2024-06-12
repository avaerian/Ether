package org.minerift.ether.benchmark;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.util.concurrent.TimeUnit;

public class EvenOrOddBenchmarks {

    public static void main(String[] args) throws RunnerException {

        Options options = new OptionsBuilder()
                .include(EvenOrOddBenchmarks.class.getSimpleName())
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
    public static class EvenOrOddState {
        @Param({
                "5",
                "-15",
                "10",
                "1000",
                "420",
                "421",
                "-69",
                "-1738"
        })
        public int num;
    }

    @Benchmark
    public boolean isEvenModulo(EvenOrOddState state) {
        // More readable, but neglibly worse
        return state.num % 2 == 0;
    }

    @Benchmark
    public boolean isEvenBitwise(EvenOrOddState state) {
        // Less readable, but slightly more performant
        return (state.num | 1) != state.num;
    }

}
