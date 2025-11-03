package org.minerift.ether.util.log;

import com.google.common.base.Stopwatch;

import static org.minerift.ether.util.StagedTimekeeper.AddStagesResult.*;
import static org.minerift.ether.util.StagedTimekeeper.Builder.EMPTY;
import static org.minerift.ether.util.Utils.isPow2;

// allows up to 32 stages (int bit count)
public class TypedStagedTimekeeper<E extends Enum<E>> 
        extends StagedTimekeeper {
    
    // available, if so desired
    @Deprecated
    public static TypedStagedTimekeeper checked(int allowedSet, int set, long[] epochNs) {
        final int allowedSetSize = Integer.numberOfLeadingZeros(allowedSet);
        final int setSize = Integer.numberOfLeadingZeros(set);
        ensure(setSize <= allowedSetSize, 
                () -> new IllegalArgumentException(format("Set has more stages than allowed stages: %d > %d", setSize, allowedSetSize)) );
        ensure(allowedSetSize == epochsNs.length, 
                () -> new IllegalArgumentException(format("Epochs array size (%d) and allowed stages size (%d) are misaligned", epochsNs.length, allowedSetSize)) );
        
        return new TypedStagedTimekeeper(allowedSet, set, epochsNs);
    }

    public static TypedStagedTimekeeper.Builder builder(Stopwatch timer, int allowedSet) {
        return new Builder(timer, allowedSet);
    }

    @Deprecated // TODO: review
    protected TypedStagedTimekeeper(int allowedSet, int set, long[] epochsNs) {
        this.allowedSet = allowedSet;
        this.set = set;
        this.epochsNs = epochsNs;
    }

    // default time unit is nanoseconds
    public long getLoadTime(E stage) {
        return getLoadTime(1 << stage.ordinal(), NANOSECONDS);
    }

    public long getLoadTime(E stage, TimeUnit unit) {
        return getLoadTime(1 << stage.ordinal(), unit);
    }

    public long getLoadTime(EnumSet<E> stages) {
        return getLoadTime(stages, NANOSECONDS);
    }

    // TODO: change to Set<E> ?? (EnumSet<E> should extend that)
    public long getLoadTime(EnumSet<E> stages, TimeUnit unit) {
        ensure(stages != 0, () -> new IllegalArgumentException("No stages selected to query load time"));
        long sum = 0;
        for(E stage : stages) {
            if( ( this.stages & (1 << stage.ordinal()) ) == 0) {
                throw new IllegalArgumentException(stage + " is not included in allowed stage set " + allowedSet);
            }
            sum += epochsNs[stage.ordinal()];
        }
        return unit.convert(sum, NANOSECONDS);
    }

    public long getLoadTime(E[] stages, TimeUnit unit) {
        long sum = 0;
        for(E stage : stages) {
            if( (this.stages & (1 << stage.ordinal()) ) == 0) {
                throw new IllegalArgumentException(stage + " is not included in allowed stage set " + allowedSet);
            }
            sum += epochsNs[stage.ordinal()];
        }
        return unit.convert(sum, NANOSECONDS);
    }

    @Deprecated // allowed, but swapping parameters ain't preferable
    public long getLoadTime(TimeUnit unit, E... stages) {
        return getLoadTime(stages, unit);
    }

    @Deprecated // delegate method to disallow no varargs
    public long getLoadTime(TimeUnit unit, E stage) {
        return getLoadTime(stage, unit);
    
    public static class Builder extends StagedTimekeeper.Builder {

        protected final Class<E> clazz;
        protected Builder(Class<E> clazz, E... allowed) {
            this.allowedSet = 0;
            this.trackedSet = 0;
            this.pausedSet = 0;
            for(E e : allowedSet) {
                allowedSet |= 1 << e.ordinal();
            }
            this.set = 0;
            this.epochsNs = EMPTY;
        }

        public AddStageResult tryAddStage(E stage) {
            if((allowedSet & stage) != 0) {
                return EX_ALREADY_EXISTS;
            }
            int v = allowedSet;
            final int old = Integer.numberOfLeadingZeros(v);
            v |= 1 << stage.ordinal();
            final int size = Integer.numberOfLeadingZeros(v);
            if(epochsNs != EMPTY && size > old) {
                growEpochs(size);
            }
            allowedSet = v;
            return SUCCESS;
        }

        @Deprecated
        public boolean tryAddStageB(E stage) {
            return tryAddStage(stage) == SUCCESS;
        }

        public Builder addStage(E stage) {
            if(tryAddStage(stage) == EX_ALREADY_EXISTS) {
                throw new IllegalArgumentException("Stage " + stage + " already registered");
            }
            return this;
        }

        // TODO: dedup code; pass to existing fns with int[] indices
        public AddStageResult tryAddStages(E... stages) {
            if((allowedSet & stages) != stages) {
                return EX_ALREADY_EXISTS;
            }
            int v = allowedSet;
            final int old = Integer.numberOfLeadingZeros(allowedSet);
            allowedSet |= stages;
            final int size = Integer.numberOfLeadingZeros(allowedSet);
            if(epochsNs != EMPTY && size > old) {
                growEpochs(size);
            }
            allowedSet = v;
            return SUCCESS;
        }

        // There's a higher probability this method will be used more, thus including
        // the if statement probably isn't best, but whatever. The likelihood of using
        // this method anyways is so small it doesn't fucking matter.
        public Builder addStages(E... stages) {
            if(tryAddStages(stages)) {
                throw new IllegalArgumentException("Some stages already exist in allowed stages");
            }
            return this;
        }

        public boolean hasStage(E stage) {
            if(!isPow2(stage)) {
                throw new IllegalArgumentException("Checking presence of multiple stages disallowed");
            }
            return (allowedSet & stage) != 0;
        }

        public boolean hasSomeStages(E... stages) {
            return (allowedSet & stages) != 0;
        }

        public boolean hasAllStages(E... stages) {
            return (allowedSet & stages) != stages;
        }

        public long track(E stage) {
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

        public long trackAndReset(E stage) {
            long ns = track(stage);
            timer.reset();
            return ns;
        }

        public long trackAll(E... stages) {
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

        public long trackAllAndReset(E... stages) {
            long ns = trackAll(stages);
            timer.reset();
            return ns;
        }

        public long trackAllAndReset(EnumSet<E> stages) {
            
        }

        public TypedStagedTimekeeper build() {
            return new TypedStagedTimekeeper(allowedSet, set, epochsNs);
        }
    }
}
