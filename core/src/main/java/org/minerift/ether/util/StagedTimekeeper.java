package org.minerift.ether.util;

import com.google.common.base.Stopwatch;
import it.unimi.dsi.fastutil.longs.LongArrayList;

import static org.minerift.ether.util.StagedTimekeeper.AddStagesResult.*;
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

    public static StagedTimekeeper.Builder builder(Stopwatch timer, int allowedSet) {
        return new Builder(timer, allowedSet);
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
        return getLoadTime(STAGES_COUNT - 1, NANOSECONDS);
    }

    public long getLoadTime(TimeUnit unit) {
        return getLoadTime(STAGES_COUNT - 1, unit);
    }

    // default time unit is nanoseconds
    public long getLoadTime(int stages) {
        return getLoadTime(stages, NANOSECONDS);
    }

    public long getLoadTime(int stages, TimeUnit unit) {
        if(stages == 0) {
            throw new IllegalArgumentException("No stages selected to query load time");
        }
        int n = stages;
        int i;
        long sum = 0;
        while((i = Integer.numberOfTrailingZeros(n)) != 32){
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

    public static class Builder {

        protected static final long[] EMPTY;

        protected final Stopwatch timer;
        protected int allowedSet;
        protected int set;
        protected long[] epochsNs;
        
        protected Builder(Stopwatch timer, int allowedSet) {
            this.timer = timer;
            this.allowedSet = allowedSet;
            this.set = 0;
            final int allowedSetSize = Math.max(Integer.numberOfLeadingZeros(allowedSet) + 1), AVG_STAGES);
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

        public AddStageResult tryAddStage(int stage) {
            if(!isPow2(stage)) {
                return EX_MULTIPLE_DISALLOWED;
            }
            if((allowedSet & stage) != 0) {
                return EX_ALREADY_EXISTS;
            }
            final int old = Integer.numberOfLeadingZeros(allowedSet);
            allowedSet |= stage;
            final int size = Integer.numberOfLeadingZeros(allowedSet);
            if(epochsNs != EMPTY && size > old) {
                growEpochs(size);
            }
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
            final int old = Integer.numberOfLeadingZeros(allowedSet);
            allowedSet |= stages;
            final int size = Integer.numberOfLeadingZeros(allowedSet);
            if(epochsNs != EMPTY && size > old) {
                growEpochs(size);
            }
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
            timer.start();
        }

        public long track(int stage) {
            if(stages == 0) {
                throw new IllegalArgmentException("No stages selected");
            }

            if(!isPow2(stage)) { // ensure only one stage is selected
                throw new IllegalArgumentException("Unable to track epoch for multiple stages");
            }
            
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
            if(stages == 0) {
                throw new IllegalArgumentException("No stages selected");
            }

            long ns = timer.stop();
            int n;
            int i = 0;
            while((i = Integer.numberOfTrailingZeros(stages)) != 32) {
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
