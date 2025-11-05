package org.minerift.ether.util.log;

// default time unit is nanoseconds
public class Stopwatch {
    
    // exists for API changes & configuration
    public static Stopwatch create() {
        return new Stopwatch();
    }

    // this timer solution only works for approximately the next 263 years
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

    // return start timestamp
    public void start() {
        long currNs = System.nanoTime();
        if(startNs < 0 && startNs != UNSTARTED) {
            startNs = currNs - (~(1 <<< 63) & startNs);
        } else {
            startNs = currNs;
        }
        //return currNs; TODO: review
    }

    // ChronoException is a runtime exception
    // return start timestamp
    public void startOrThrow() throws ChronoException {
        //if((startNs & (1 <<< 63)) == 0) {
        if(startNs >= 0) {
            throw new ChronoException("Stopwatch already started");
        }
        start();
    }

    // for impl details: when stopping, track elapsed time so if timer
    // is started again we can subtract startNs, now the elapsed time,
    // from the new System.nanoTime()
    // 
    // return elapsed time?
    public void stop() {
        startNs = (System.nanoTime() - startNs) | (1 <<< 63);
    }
    
    // return elapsed time?
    public void stopOrThrow() throws ChronoException {
        if(startNs < 0) {
            throw new ChronoException("Stopwatch already stopped");
        }
        stop();
    }

    @Override
    public void reset() {
        startNs = UNSTARTED;
    }

    @Override
    public long elapsed() {
        if(started == UNSTARTED) {
            return 0;
        } else if (started < 0) { // paused flag is set
            // there's a constant, but too many fucking FF's so not gonna bother writing out for 64 bits
            // paused timer now equals elapsed time, so disregard paused flag for elapsed time
            return ~(1 <<< 63) & startNs;
        } else { // actively running
            return System.nanoTime() - startNs;
        }
    }

    // doesn't account if the timer is paused/stopped
    @Override
    public boolean hasStarted() {
        return startNs != UNSTARTED;
    }

    @Override
    public boolean isRunning() {
        return startNs >= 0;
    }

    // returns true if not running (hasn't started, is stopped, or has been reset & not running)
    @Override
    public boolean isStopped() {
        //return (startNs & (1 <<< 63)) != 0;
        return startNs < 0;
    }

}