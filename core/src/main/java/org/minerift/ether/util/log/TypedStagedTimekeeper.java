package org.minerift.ether.util.log;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

import static org.minerift.ether.util.StagedTimekeeper.StagesOpResult.*;
import static org.minerift.ether.util.StagedTimekeeper.Builder.EMPTY;
//import static org.minerift.ether.util.Utils.isPow2;

import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

// allows up to 32 stages (int bit count)
// TODO: behavior and methods need to be revie
public class TypedStagedTimekeeper<E extends Enum<E>> extends StagedTimekeeper {
    
    // TODO: static methods for checked with E[], EnumSet<E> ?

    
    public static <E extends Enum<E>> TypedStagedTimekeeper checked(Class<E> clazz, E[] allowedSet, E[] set, long[] epochNs) {
        EnumSet<E> _allowedSet = EnumSet.copyOf(Arrays.asList(allowedSet));
        EnumSet<E> _set = EnumSet.copyOf(Arrays.asList(set));

        ensure(_set.containsAll(_allowedSet),  () -> new IllegalArgumentException("Items in set aren't being tracked"));
        ensure(_set.size() == epochsNs.length, () -> new IllegalArgumentException(/*format("A*/));
        
        return new TypedStagedTimekeeper(_allowedSet, _set, epochsNs);
    }

    // available, if so desired
    public static <E extends Enum<E>> TypedStagedTimekeeper checked(EnumSet<E> allowedSet, EnumSet<E> set, long[] epochsNs) {
        ensure(_set.size() == epochsNs.length, () -> new IllegalArgumentException(/*format("*/));
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

        public StagesOpResult tryAddStage(E stage) {
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
        public StagesOpResult tryAddStages(E... stages) {
            int v = allowedSet;
            final int old = Integer.numberOfLeadingZeros(v);
            for(E stage : stages) {
                int i = 1 << stage.ordinal();
                if((allowedSet & i) != 0) {
                    return EX_ALREADY_EXISTS;
                }
                v |= i;
            }
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
        public Builder addStages(E... stages) {
            if(tryAddStages(stages)) {
                throw new IllegalArgumentException("Some stages already exist in allowed stages");
            }
            return this;
        }

        public boolean hasStage(E stage) {
            return hasStage(1 << stage.ordinal());
        }

        public boolean hasSomeStages(E... stages) {
            int set = 0;
            for(E stage : stages) {
                set |= 1 << stage.ordinal();
            }
            return hasSomeStages(set);
        }

        public boolean hasAllStages(E... stages) {
            int set = 0;
            for(E stage : stages) {
                set |= 1 << stage.ordinal();
            }
            return hasAllStages(set);
        }

        public long track(E stage) {
            /*if(stages == 0) {
                throw new IllegalArgmentException("No stages selected");
            }*/
            
            timer.stop();
            long ns = timer.elapsed();
            epochsNs[stage.ordinal()] += ns;
            return ns;
        }

        public long track(E... stages) {
            /*if(stages == 0) {
                throw new IllegalArgumentException("No stages selected");
            }*/

            timer.stop();
            long ns = timer.elapsed();
            for(E stage : stages) {
                epochsNs[stage.ordinal()] += ns;
            }
            return ns;
        }

        public long track(EnumSet<E> stages) {
            timer.stop();
            long ns = timer.elapsed();
            for(E stage : stages) {
                epochsNs[stage.ordinal()] += ns;
            }
            return ns;
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
