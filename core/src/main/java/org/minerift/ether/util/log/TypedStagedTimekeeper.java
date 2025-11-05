package org.minerift.ether.util.log;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

import static org.minerift.ether.util.StagedTimekeeper.AddStagesResult.*;
import static org.minerift.ether.util.StagedTimekeeper.Builder.EMPTY;
import static org.minerift.ether.util.Utils.isPow2;

import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

// allows up to 32 stages (int bit count)
public class TypedStagedTimekeeper<E extends Enum<E>> 
        extends StagedTimekeeper {
    
    // TODO: static methods for checked with E[], EnumSet<E> ?

    
    public static <E extends Enum<E>> TypedStagedTimekeeper checked(Class<E> clazz, E[] allowedSet, E[] set, long[] epochNs) {
        EnumSet<E> _allowedSet = EnumSet.copyOf(Arrays.asList(allowedSet));
        EnumSet<E> _set = EnumSet.copyOf(Arrays.asList(set));

        ensure(_set.containsAll(_allowedSet),  () -> new IllegalArgumentException("Items in set aren't being tracked"));
        ensure(_set.size() == epochsNs.length, () -> new IllegalArgumentException(format("A
        
        return new TypedStagedTimekeeper(_allowedSet, _set, epochsNs);
    }

    // available, if so desired
    public static <E extends Enum<E>> TypedStagedTimekeeper checked(EnumSet<E> allowedSet, EnumSet<E> set, long[] epochsNs) {
        ensure(_set.size() == epochsNs.length, () -> new IllegalArgumentException(format(" 
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
        return getLoadTime(NANOSECONDS, 1 << stage.ordinal());
    }

    public long getLoadTime(TimeUnit unit, E stage) {
        return getLoadTime(unit, 1 << stage.ordinal());
    }

    public long getLoadTime(EnumSet<E> stages) {
        return getLoadTime(NANOSECONDS, stages);
    }

    // TODO: change to Set<E> ?? (EnumSet<E> should extend that)
    public long getLoadTime(TimeUnit unit, EnumSet<E> stages) {
        ensure(stages.size() != 0, () -> new IllegalArgumentException("No stages selected to query load time"));
        long sum = 0;
        for(E stage : stages) {
            if( ( this.stages & (1 << stage.ordinal()) ) == 0) {
                throw new IllegalArgumentException(stage + " is not included in allowed stage set " + allowedSet);
            }
            sum += epochsNs[stage.ordinal()];
        }
        return unit.convert(sum, NANOSECONDS);
    }

    public long getLoadTime(TimeUnit unit, E... stages) {
        long sum = 0;
        for(E stage : stages) {
            if( (this.stages & (1 << stage.ordinal()) ) == 0) {
                throw new IllegalArgumentException(stage + " is not included in allowed stage set " + allowedSet);
            }
            sum += epochsNs[stage.ordinal()];
        }
        return unit.convert(sum, NANOSECONDS);
    }
    
    public static class Builder extends StagedTimekeeper.Builder {

        protected final Class<E> clazz;
        protected Builder(Class<E> clazz, E... allowed) {
            this.allowedSet = 0;
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
            
            timer.stop();
            int i = Integer.numberOfTrailingZeros(stage);
            long ns = timer.elapsed();
            epochsNs[i] = ns;
            return ns;
        }

        public long track(E... stages) {
            if(stages == 0) {
                throw new IllegalArgumentException("No stages selected");
            }

            timer.stop();
            long ns = timer.elapsed();
            int n;
            int i = 0;
            while((i = Integer.numberOfTrailingZeros(stages)) != 32) {
                epochsNs[i] = ns;
                n &= ~(1 << i);
            }
            return ns;
        }

        public long track(EnumSet<E> stages) {
            long sum = 0;
            for(E e : stages) {
                sum += epochsNs[e.ordinal()];
            }
            return sum;
        }

        public long trackSafe(EnumSet<E> stages) {
            long sum = 0;
            for(E e : stages) {
                if( (allowedSet & (1 << e.ordinal()) ) == 0) {
                    throw new IllegalArgumentException("Stage " + e + " not an allowed stage");
                }
                sum += epochsNs[e.ordinal()];
            }
            return sum;
        }

        public long trackAndReset(E stage) {
            long ns = track(stage);
            timer.reset();
            return ns;
        }

        public long trackAndReset(TimeUnit unit, E stage) {
            return unit.convert(trackandReset(stage), NANOSECONDS);

        public long trackAndReset(E... stages) {
            long ns = track(stages);
            timer.reset();
            return ns;
        }

        public long trackAndReset(EnumSet<E> stages) {
            long ns = track(stages);
            timer.reset();
            return ns;
        }

        public TypedStagedTimekeeper build() {
            return new TypedStagedTimekeeper(allowedSet, set, epochsNs);
        }
    }
}
