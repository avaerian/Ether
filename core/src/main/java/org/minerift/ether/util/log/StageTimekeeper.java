package org.minerift.ether.util.log;

import org.minerift.ether.util.fn.Exceptional;

import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

// TODO: for bit impl, copy class and use commit history for reference
public class StageTimekeeper<E extends Enum<E>> /*implements Iterator<Entry>*/ {

    protected final Stopwatch timer;
    protected final Class<E> clazz;
    protected final E[] enums;
    protected final long[] epochsNs;

    public StageTimekeeper(Class<E> clazz, Stopwatch stopwatch) {
        this.timer = stopwatch;
        this.clazz = clazz;
        this.enums = clazz.getEnumConstants();
        this.epochsNs = new long[enums.length];
    }

    public StageTimekeeper(Class<E> clazz) {
        this(clazz, Stopwatch.create());
    }

    public long getLoadTime() {
        return getLoadTime(NANOSECONDS, enums);
    }

    public long getLoadTime(TimeUnit unit) {
        return getLoadTime(unit, enums);
    }

    // default time unit is nanoseconds
    @SafeVarargs
    public final long getLoadTime(E... stages) {
        return getLoadTime(NANOSECONDS, stages);
    }

    @SafeVarargs
    public final long getLoadTime(TimeUnit unit, E... stages) {
        long sum = 0;
        for (E stage : stages)
            sum += epochsNs[stage.ordinal()];
        return unit.convert(sum, NANOSECONDS);
    }

    public void start() {
        timer.start();
    }

    // ChronoException is a runtime exception
    public void startOrThrow() throws ChronoException {
        timer.startOrThrow();
    }

    public void stop() {
        timer.stop();
    }

    public void stopOrThrow() throws ChronoException {
        timer.stopOrThrow();
    }

    public void reset() {
        timer.reset();
    }

    public long elapsed() {
        return timer.elapsed();
    }

    public Stopwatch getStopwatch() {
        return timer;
    }

    // returns the current stage's elapsed time
    public long track(E stage) {
        timer.stop();
        return epochsNs[stage.ordinal()] += timer.elapsed();
    }

    public long track(E stage, TimeUnit unit) {
        return unit.convert(track(stage), NANOSECONDS);
    }

    public long trackAndReset(E stage) {
        long elapsedNs = track(stage);
        timer.reset();
        return elapsedNs;
    }

    public long trackAndReset(E stage, TimeUnit unit) {
        return unit.convert(trackAndReset(stage), NANOSECONDS);
    }

    public long trackAll(E... stages) {
        if(stages.length != 0) throw new IllegalArgumentException("No stages selected");

        timer.stop();
        for(E stage : stages)
            epochsNs[stage.ordinal()] += timer.elapsed();
        return timer.elapsed();
    }

    public long trackAll(E[] stages, TimeUnit unit) {
        return unit.convert(trackAll(stages), NANOSECONDS);
    }

    public long trackAllAndReset(E... stages) {
        long elapsedNs = trackAll(stages);
        timer.reset();
        return elapsedNs;
    }

    public long trackAllAndReset(E[] stages, TimeUnit unit) {
        return unit.convert(trackAllAndReset(stages), NANOSECONDS);
    }

    // for measure methods, reset & start timer, run op, and track stage(s)
    public long measureStage(E stage, Runnable run) {
        timer.start();
        try {
            run.run();
        } catch (RuntimeException e) {
            throw new ChronoException("Runtime exception thrown while measuring stage", e);
        }
        timer.stop();
        long ns = timer.elapsed();
        epochsNs[stage.ordinal()] += ns;
        return ns;
    }

    public long measureStage(E stage, TimeUnit unit, Runnable run) {
        return unit.convert(measureStage(stage, run), NANOSECONDS);
    }

    // TODO: review
    public long measureStage(E stage, Exceptional run) throws ChronoException {
        timer.start();
        try {
            run.run();
        } catch (Exception e) {
            throw new ChronoException("Exception thrown while measuring stage", e);
        }
        timer.stop();
        long ns = timer.elapsed();
        epochsNs[stage.ordinal()] += ns;
        return ns;
    }

    public long measureStage(TimeUnit unit, E stage, Exceptional run) {
        return unit.convert(measureStage(stage, run), NANOSECONDS);
    }

    // TODO: switch iterator to entry iterator with stage and measured time
    /*@Override
    public @NotNull Iterator<E> iterator() {
        return new Iterator<>() {
            public int i = 0;

            @Override
            public E next() {
                return enums[i++];
            }

            @Override
            public boolean hasNext() {
                return i < enums.length;
            }
        };
    }*/

    // TODO
    /*public static class View<E extends Enum<E>> {

        protected int allowedSet;
        protected int set; // stages finished tracking
        protected long[] epochsNs;

        protected Builder(int allowedSet) {
            this.allowedSet = allowedSet;
            this.set = 0;
            this.epochsNs = EMPTY;
        }

        public StageTimekeeper build() {
            return new StageTimekeeper(allowedSet, set, epochsNs);
        }
    }*/
}
