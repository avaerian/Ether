package org.minerift.ether.util.log;

//import com.google.common.base.Stopwatch;
//import it.unimi.dsi.fastutil.longs.LongIterator;

import org.minerift.ether.util.fn.Exceptional;

import java.util.Iterator;
import java.util.PrimitiveIterator;

import static org.minerift.ether.util.StagedTimekeeper.StagesOpResult.*;
import static org.minerift.ether.util.Utils.ensure;
import static org.minerift.ether.util.Utils.isPow2;

// allows up to 32 stages (int bit count)
public class StagedTimekeeper implements Iterable<Long> {
    
    public static final int AVG_STAGES = 8; // not expecting too many stages

    public static int getMaxStages() {
        return Integer.SIZE;
    }
    
    // available, if so desired
    public static StagedTimekeeper checked(int allowedSet, int set, long[] epochNs) {
        final int allowedSetSize = Integer.numberOfLeadingZeros(allowedSet);
        final int setSize = Integer.numberOfLeadingZeros(set);
        ensure(setSize <= allowedSetSize, 
                () -> new IllegalArgumentException(format("Set has more stages than allowed stages: %d > %d", setSize, allowedSetSize)) );
        ensure(allowedSetSize == epochsNs.length, 
                () -> new IllegalArgumentException(format("Epochs array size (%d) and allowed stages size (%d) are misaligned", epochsNs.length, allowedSetSize)) );
        
        return new StagedTimekeeper(allowedSet, set, epochsNs);
    }

    public static StagedTimekeeper.Builder builder(int allowedSet) {
        return new Builder(allowedSet);
    }

    // internals are public; use at own risk
    public final int allowedSet; // allowed values
    public final int set; // stages with actual elapsed values
    public final long[] epochsNs;

    // no validation by default
    public StagedTimekeeper(int allowedSet, int set, long[] epochsNs) {
        this.allowedSet = allowedSet;
        this.set = set;
        this.epochsNs = epochsNs;
    }

    public class EpochsIter implements PrimitiveIterator.OfLong {
        public int n, i;

        // n -> stages set to iterate
        // should name better, but these are internals; this should do
        Iterator(int n) {
            this.n = n;
        }

        @Override
        public long nextLong() {
            /*long l = epochsNs[i];
            n &= ~(1 << i); // can this be moved?
            return l;*/
            n &= ~(1 << i);
            return epochsNs[i];
        }
        
        public int currentStage() {
            return 1 << stage;
        }

        public int currentIndex() {
            return i;
        }

        @Override
        public boolean hasNext() {
            return (i = Integer.numberOfTrailingZeros(n)) != getMaxStages();
        }
    }
    
    public EpochsIter epochsIter() {
        return new EpochsIter(set);
    }

    public EpochsIter epochsIter(int stages) {
        return new EpochsIter(stages);
    }
 
    public EpochsIter epochsIterSafe(int stages) {
        ensure((set & stages) != 0, () -> new IllegalArgumentException("Stages queried not in set of tracked stages"));
        return new EpochsIter(stages);
    }

    @Deprecated
    @Override
    public Iterator<Long> iterator() {
        return new IteratorImpl(set);
    }

    public long getLoadTime() {
        return getLoadTime(NANOSECONDS, set);
    }

    public long getLoadTime(TimeUnit unit) {
        return getLoadTime(unit, set);
    }

    // default time unit is nanoseconds
    public long getLoadTime(int stages) {
        return getLoadTime(NANOSECONDS, stages);
    }

    public long getLoadTime(TimeUnit unit, int stages) {
        ensure(stages != 0, () -> new IllegalArgumentException("No stages selected to query load time"));
        int n = stages;
        int i;
        long sum = 0;
        while((i = Integer.numberOfTrailingZeros(n)) != getMaxStages()) {
            sum += epochsNs[i];
            n &= ~(1 << i);
        }
        return unit.convert(sum, NANOSECONDS);
    }

    // TODO: review; not too keen on this impl
    public static enum StagesOpResult {
        EX_MULTIPLE_DISALLOWED,
        EX_ALREADY_EXISTS,
        EX_NO_EXISTS,
        SUCCESS,
    }
    
    // StagedTimekeeper usage has been reviewed and been concluded that the usage should be made to
    // be made simpler. Each stage will be tracked individually, which is done by starting the timer,
    // "tracking" the elapsed time after the stage completes, optionally resetting the timer and continuing
    // to track the elapsed time for all stages before submitting to an immutable view. In the immutable
    // view the user can then query the elapsed times of either specific states or a combination of states.
    // This construct assumes single-threaded, sequential stage loading, rather than multiple stages being
    // tracked simultaneously. A separate implementation can exist for that purpose, but that's not my problem.
    public static class Builder {
        protected static final long[] EMPTY = new long[0];
        protected static final long UNSTARTED = -1; // timer flag; if more flags, create mask
        
        protected int startNs; // timer; may not reflect accurate starting timestamp if pa
        protected int allowedSet;
        protected int set; // stages finished tracking
        protected long[] epochsNs;

        protected Builder(int allowedSet) {
            this.startNs = UNSTARTED;
            this.allowedSet = allowedSet;
            this.set = 0;
            this.epochsNs = EMPTY;
        }

        // expects size to be larger than current epochsNs len
        // protected access to allow user to interface with if so desired
        protected void growEpochs(int size) {
            long ls = 0;
            for(long l : epochsNs) {
                ls |= l;
            }
            if(ls != 0) { // if array is not zeroed out, copy
                long[] copy = new long[size];
                System.arraycopy(epochsNs, copy, 0, epochsNs.length, 0, epochsNs.length);
                epochsNs = copy;
            } else {
                epochsNs = new long[size];
            }
        }
         
        public StagesOpResult tryAddStage(int stage) {
            if(!isPow2(stage)) {
                return EX_MULTIPLE_DISALLOWED;
            }
            int v = allowedSet;
            if((v & stage) != 0) {
                return EX_ALREADY_EXISTS;
            }
            
            final int old = Integer.numberOfLeadingZeros(v);
            v |= stage;
            final int size = Integer.numberOfLeadingZeros(v);
            if(epochsNs != EMPTY && size > old) {
                growEpochs(size);
            }
            allowedSet = v;
            return SUCCESS;
        }

        @Deprecated
        public boolean tryAddStageB(int stage) {
            return tryAddStage(stage) == SUCCESS;
        }

        public Builder addStage(int stage) {
            switch(tryAddStage(stage)) {
                case EX_MULTIPLE_DISALLOWED -> throw new IllegalArgumentException("Unable to add multiple stages");
                case EX_ALREADY_EXISTS -> throw new IllegalArgumentException("Stage already registered");
            }
            return this;
        }

        public StagesOpResult tryAddStages(int stages) {
            if((allowedSet & stages) != stages) {
                return EX_ALREADY_EXISTS;
            }
            int v = allowedSet;
            final int old = Integer.numberOfLeadingZeros(v);
            v |= stages;
            final int size = Integer.numberOfLeadingZeros(v);
            if(epochsNs != EMPTY && size > old) {
                growEpochs(size);
            }
            allowedSet = v;
            return SUCCESS;
        }

        // There's a higher probability this method will be used more, thus including
        // the if statement probably isn't best, but whatever. The likelihood of using
        // this method anyways is so small it doesn't fucking matter.
        public Builder addStages(int stages) {
            if(tryAddStages(stages)) {
                throw new IllegalArgumentException("Some stages already exist in allowed stages");
            }
            return this;
        }

        public boolean hasStage(int stage) {
            if(!isPow2(stage)) {
                throw new IllegalArgumentException("Checking presence of multiple stages disallowed");
            }
            return (allowedSet & stage) != 0;
        }

        public boolean hasSomeStages(int stages) {
            return (allowedSet & stages) != 0;
        }

        public boolean hasAllStages(int stages) {
            return (allowedSet & stages) != stages;
        }

        // this timer solution only works for approximately the next 263 years
        // for nanoseconds as our time unit, so if we want to extend the
        // allowed amount of time we could probably just add a separate 
        // flag variable or something, but this is fine for now (or, even 
        // worse, a whole new data structure; how about a long[] for 
        // additional bits?)

        public void start() {
            if(startNs < 0 && startNs != UNSTARTED) {
                startNs = System.nanoTime() - (~(1 <<< 63) & startNs);
            } else {
                startNs = System.nanoTime();
            }
        }

        // TimerException is a runtime exception
        public void startOrThrow() throws TimerException {
            //if((startNs & (1 <<< 63)) == 0) {
            if(startNs >= 0) {
                throw new TimerException("Timer already started");
            }
            start();
        }

        // for impl details: when stopping, track elapsed time so if timer
        // is started again we can subtract startNs, now the elapsed time,
        // from the new System.nanoTime()
        public void stop() {
            startNs = (System.nanoTime() - startNs) | (1 <<< 63);
        }
        
        public void stopOrThrow() {
            if(startNs < 0) {
                throw new TimerException("Timer already stopped");
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

        public long track(int stage) {
            ensure(allowedSet != 0, () -> new IllegalArgmentException("No stages selected"));
            ensure(isPow2(stage), () -> new IllegalArgumentException("Unable to track epoch for multiple stages"));
            ensure((allowedSet & stage) == stage, () -> new IllegalArgumentException("Unable to track stage excluded from stage set"));
              
            int i = Integer.numberOfTrailingZeros(stage);
            long endNs = System.nanoTime();
            epochsNs[i] = endNs - startNs;
        }

        // for measure methods, reset & start timer, run op, and track stage(s)
        public long measureStage(int stage, Runnable run) {
            // TODO: figure out startNs resetting or retaining
            
        }

        // TODO: review
        public <E extends Exception> long measureStage(int stage, Exceptional<E> run) throws E {
            
        }

        public long trackAndReset(int stage) {
            long elapsedNs = track(stage);
            startNs = UNSTARTED;
            return elapsedNs;
        }

        public long trackAll(int stages) {
            ensure(stages != 0, () -> new IllegalArgumentException("No stages selected"));
            ensure((this.stages & stages) == stages, () -> new IllegalArgumentException("Stage excluded from tracked stage set"));

            long endNs = System.nanoTime();
            int n = stages;
            int i;
            while((i = Integer.numberOfTrailingZeros(n)) != StagedTimekeeper.getMaxStages()) {
                synchronized(epochsNs[i]) {
                    if(epochsNs[i] != 0) {
                        epochsNs[i] = endNs - epochsNs[i];
                    } else {
                        epochsNs[i] = endNs - startNs;
                    }
                }
                n &= ~(1 << i);
            }
            return ns;
        }

        public long trackAllAndReset(int stages) {
            long elapsedNs = trackAll(stages);
            startNs = UNSTARTED;
            return elapsedNs;
        }

        public StagedTimekeeper build() {
            return new StagedTimekeeper(allowedSet, set, epochsNs);
        }
    }
}
