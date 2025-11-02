package org.minerift.ether.util;

//import com.google.common.base.Stopwatch;

import static org.minerift.ether.util.StagedTimekeeper.AddStagesResult.*;
import static org.minerift.ether.util.Utils.ensure;
import static org.minerift.ether.util.Utils.isPow2;

// allows up to 32 stages (int bit count)
public class StagedTimekeeper {
    
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
    public final int allowedSet;
    public final int set;
    public final long[] epochsNs;

    public StagedTimekeeper(int allowedSet, int set, long[] epochsNs) {
        this.allowedSet = allowedSet;
        this.set = set;
        this.epochsNs = epochsNs;
    }

    public long getLoadTime() {
        return getLoadTime(set, NANOSECONDS);
    }

    public long getLoadTime(TimeUnit unit) {
        return getLoadTime(set, unit);
    }

    // default time unit is nanoseconds
    public long getLoadTime(int stages) {
        return getLoadTime(stages, NANOSECONDS);
    }

    public long getLoadTime(int stages, TimeUnit unit) {
        ensure(stages != 0, () -> new IllegalArgumentException("No stages selected to query load time"));
        int n = stages;
        int i;
        long sum = 0;
        while((i = Integer.numberOfTrailingZeros(n)) != getMaxStages()){
            sum += epochsNs[i];
            n &= ~(1 << i);
        }
        return unit.convert(sum, NANOSECONDS);
    }

    public static enum AddStagesResult {
        EX_MULTIPLE_DISALLOWED,
        EX_ALREADY_EXISTS,
        SUCCESS,
    }

    // TODO: update with async impl in-mind
    //  - keep track of tracked stages
    //  - when tracking a stage, store init timestamp in array
    //  - on start(), store long lastStart
    //  - on track(), for each tracked stage subtract current timestamp from timestamp in array
    public static class Builder {

        protected static final long[] EMPTY = new long[0];
        protected static final long UNSTARTED = -1;
        
        protected volatile int allowedSet;
        protected volatile int trackedSet; // stages being actively tracked
        protected volatile int set; // stages finished tracking
        protected volatile long[] epochsNs;
        protected volatile long startedNs;
        
        protected Builder(int allowedSet) {
            this.timer = timer;
            this.allowedSet = allowedSet;
            this.trackedSet = 0;
            this.set = 0;
            this.epochsNs = EMPTY;
            this.startedNs = UNSTARTED;
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

        public AddStageResult tryAddStage(int stage) {
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

        public AddStageResult tryAddStages(int stages) {
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

        // no point in returning Builder for this
        public void start() {
            ensure(startedNs == UNSTARTED, () -> new IllegalStateException("Stopwatch already started");
            this.startedNs = System.nanoTime();
        }

        // start stopwatch for specific stages
        public void start(int stages) {
            ensure(startedNs == UNSTARTED, () -> new IllegalStateException("Stopwatch already started");
            ensure((this.stages & stages) == stages, 
                    () -> new IllegalArgumentException("Attempted to start stopwatch for invalid/unregistered states");
            ensure(stages != 0, () -> new IllegalArgumentException("No stages selected to start stopwatch for");
            int n = stages;
            int i;
            long ns = System.nanoTime();
            while((i = Integer.numberOfTrailingZeros(n) != StagedTimekeeper.getMaxStages()) {
                epochsNs[i] = ns;
                n &= ~(1 << i);
            }
        }

        public long track(int stage) {
            ensure((stages != 0), () -> new IllegalArgmentException("No stages selected"));
            ensure(isPow2(stage), () -> new IllegalArgumentException("Unable to track epoch for multiple stages"));
            ensure((stages & stage) == stage, () -> new IllegalArgumentException("Unable to track stage excluded from stage set"));
            
            int i = Integer.numberOfTrailingZeros(stage);
            long ns = timer.stop();
            epochsNs[i] = ns;
            return ns;
        }

        public long trackAndReset(int stage) {
            long ns = track(stage);
            timer.reset();
            return ns;
        }

        public long trackAll(int stages) {
            ensure(stages != 0, () -> new IllegalArgumentException("No stages selected"));
            ensure((this.stages & stages) == stages, () -> new IllegalArgumentException("Stage excluded from tracked stage set"));

            long ns = timer.stop();
            int n = stages;
            int i;
            while((i = Integer.numberOfTrailingZeros(n)) != 32) {
                epochsNs[i] = ns;
                n &= ~(1 << i);
            }
            return ns;
        }

        public long trackAllAndReset(int stages) {
            long ns = trackAll(stages);
            timer.reset();
            return ns;
        }

        public StagedTimekeeper build() {
            return new StagedTimekeeper(allowedSet, set, epochsNs);
        }
    }
}
