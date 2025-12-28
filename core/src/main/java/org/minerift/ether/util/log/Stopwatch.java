package org.minerift.ether.util.log;

import org.minerift.ether.debug.NeedsUnitTests;

// default time unit is nanoseconds
@NeedsUnitTests
public class Stopwatch {
    
    // exists for API changes & configuration
    public static Stopwatch create() {
        return new Stopwatch();
    }

    // This timer solution only works for approximately the next 237 years
    // for nanoseconds as our time unit, so if we want to extend the
    // allowed amount of time we could probably just add a separate 
    // flag variable or something, but this is fine for now (or, even 
    // worse, a whole new data structure; how about a long[] for 
    // additional bits?)

    public static final long UNSTARTED = -1; // timer flag; for more, create flags mask

    protected long startNs;
    public Stopwatch() {
        this.startNs = UNSTARTED;
    }

    /**
     * Start the stopwatch.
     *
     * @return timestamp of when this stopwatch was started, in nanoseconds.
     */
    public long start() {
        long currNs;
        if(startNs < 0 && startNs != UNSTARTED) {
            startNs = (currNs = System.nanoTime()) - (~(1L << 63) & startNs);
        } else {
            startNs = (currNs = System.nanoTime());
        }
        return currNs;
    }

    /**
     * Start the stopwatch, or throw if stopwatch is already running.
     *
     * @return timestamp of when this stopwatch was started, in nanoseconds.
     *
     * @throws ChronoException if stopwatch is already running.
     */
    public long startOrThrow() throws ChronoException {
        if(startNs >= 0) {
            throw new ChronoException("Stopwatch already started");
        }
        return start();
    }

    /**
     * Stop the stopwatch.
     *
     * <p>
     * Implementation details: stored value is the elapsed time so if started again
     * we can subtract the elapsed time from the new {@code System.nanoTime()} start timestamp.
     *
     * @return timestamp of when this stopwatch was stopped, in nanoseconds.
     */
    public long stop() {
        long currNs = System.nanoTime();
        startNs = (currNs - startNs) | (1L << 63);
        return currNs;
    }

    /**
     * Stop the stopwatch, or throw if stopwatch isn't running.
     *
     * <p>
     * Implementation details: stored value is the elapsed time so if started again
     * we can subtract the elapsed time from the new {@code System.nanoTime()} start timestamp.
     *
     * @return timestamp of when this stopwatch was stopped, in nanoseconds.
     *
     * @throws ChronoException if stopwatch isn't running, or is already stopped.
     */
    public long stopOrThrow() throws ChronoException {
        if(startNs < 0) {
            throw new ChronoException("Stopwatch already stopped");
        }
        return stop();
    }

    /**
     * Reset the stopwatch.
     */
    public void reset() {
        startNs = UNSTARTED;
    }

    /**
     * Returns the current elapsed time of the stopwatch.
     *
     * @return the elapsed time, in nanoseconds.
     */
    public long elapsed() {
        if(startNs == UNSTARTED) {
            return 0;
        } else if (startNs < 0) { // paused flag is set
            // there's a constant, but too many fucking FF's so not gonna bother writing out for 64 bits
            // paused timer now equals elapsed time, so disregard paused flag for elapsed time
            return ~(1L << 63) & startNs;
        } else { // actively running
            return System.nanoTime() - startNs;
        }
    }

    // doesn't account if the timer is paused/stopped

    /**
     * Returns whether the stopwatch has been started.
     *
     * @return if the stopwatch has been started. Can return
     * true even if the stopwatch is stopped, as long as
     * there's some elapsed time. In other words, doesn't
     * account if the timer is stopped; as long as the
     * stopwatch was started.
     */
    public boolean hasStarted() {
        return startNs != UNSTARTED;
    }

    /**
     * Returns whether the stopwatch is actively running.
     *
     * @return if the stopwatch is running.
     */
    public boolean isRunning() {
        return startNs >= 0;
    }

    // returns true if not running (hasn't started, is stopped, or has been reset & not running)

    /**
     * Returns whether the stopwatch is stopped.
     *
     * @return if the stopwatch is stopped; not running.
     * Equivalent to using {@code !isRunning()}.
     */
    public boolean isStopped() {
        return startNs < 0;
    }

}