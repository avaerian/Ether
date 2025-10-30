package org.minerift.ether.util;

import static org.minerift.ether.util.Utils.isPow2;

public class Timekeeper {
    // internals are public; use at own risk
    public final int allowedSet;
    public final int set;
    public final long[] epochsNs;

    public Timekeeper(int allowedSet, int set, long[] epochsNs) {
        this.allowedSet = allowedSet;
        this.set = set;
        this.epochsNs = epochsNs;
    }

    // not a fan of this, but don't want to write this out every goddamn time
    @Deprecated
    protected static int index(int stage) {
        return Integer.numberOfTrailingZeros(stage);
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

    public static class Builder {
        protected int allowedSet;
        protected int set;
        protected long[] epochsNs;
        
        protected Builder(int allowedSet) {
            this.allowedSet = allowedSet;

        public Builder addStage(int stage) {
            if(!isPow2(stage)) {
                
            }
            if((allowedSet & stage) != 0)
        }

        @NeedsTesting
        public long register(int stage, long epochNs) {
            if(stage == 0) {
                throw new IllegalArgumentException("No stages selected to register epoch");
            }
            if( (stage & (stage - 1)) != 0 ) { // ensure only one stage is selected; check if pow of 2
                throw new IllegalArgumentException("Unable to register epoch for multiple stages");
            }
            
            int i = Integer.numberOfTrailingZeros(stage);
            epochsNs[i] = epochNs;
            return epochNs;
        }

        public Timekeeper build() {
            return new Timekeeper(allowedSet, set, epochsNs);
        }
    }
}