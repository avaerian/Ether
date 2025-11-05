package org.minerift.ether.util.log;

// default time unit is nanoseconds
public interface Stopwatch {
    
    static Stopwatch create() {
        return new Impl();
    }

    void start(); // return timestamp of start?
    void stop(); // return timestamp of stop?
    void reset();
    long elapsed();

    void startOrThrow() throws ChronoException;
    void stopOrThrow() throws ChronoException;

    // this timer solution only works for approximately the next 263 years
    // for nanoseconds as our time unit, so if we want to extend the
    // allowed amount of time we could probably just add a separate 
    // flag variable or something, but this is fine for now (or, even 
    // worse, a whole new data structure; how about a long[] for 
    // additional bits?)
    class Impl {
        protected static final long UNSTARTED = -1; // timer flag; for more, create flags mask

        protected long startNs;
        Impl() {
            this.startNs = UNSTARTED;
        }

        public void start() {
            if(startNs < 0 && startNs != UNSTARTED) {
                startNs = System.nanoTime() - (~(1 <<< 63) & startNs);
            } else {
                startNs = System.nanoTime();
            }
        }
    
        // ChronoException is a runtime exception
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
        public void stop() {
            startNs = (System.nanoTime() - startNs) | (1 <<< 63);
        }
        
        public void stopOrThrow() throws ChronoException {
            if(startNs < 0) {
                throw new ChronoException("Stopwatch already stopped");
            }
            stop();
        }
    
        public void reset() {
            startNs = UNSTARTED;
        }
    
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
    }

}