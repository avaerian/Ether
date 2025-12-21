package org.minerift.ether.util.log;

import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.minerift.ether.util.Utils.ensure;
import static org.minerift.ether.util.log.StagedTimekeeper.StagesOpResult.EX_ALREADY_EXISTS;
import static org.minerift.ether.util.log.StagedTimekeeper.StagesOpResult.SUCCESS;

// allows up to 32 stages (int bit count)
// TODO: behavior and methods need to be reviewed
public class TypedStagedTimekeeper<E extends Enum<E>> extends StagedTimekeeper {
    
    public static <E extends Enum<E>> TypedStagedTimekeeper<E> checked(/*Class<E> clazz, */E[] allowedSet, E[] set, long[] epochsNs) {
        int _allowedSet = 0;
        int _set = 0;

        for(E e : allowedSet) {
            _allowedSet |= 1 << e.ordinal();
        }

        int setSize = 0;
        for(E e : set) {
            int i = 1 << e.ordinal();
            if((_set & i) == 0) {
                setSize++;
            }
            if((_allowedSet & i) == 0) {
                throw new IllegalArgumentException("Items in set aren't in allowed set/tracked stages");
            }
            _set |= i;
        }

        if(setSize != epochsNs.length) {
            throw new IllegalArgumentException(format("Mismatched set (%d) and epochs (%d) sizes", setSize, epochsNs.length));
        }

        return new TypedStagedTimekeeper<>(_allowedSet, _set, epochsNs);
    }

    // available, if so desired
    public static <E extends Enum<E>> TypedStagedTimekeeper<E> checked(Set<E> allowedSet, Set<E> set, long[] epochsNs) {
        E[] _allowedSet = (E[])allowedSet.toArray();
        E[] _set = (E[])set.toArray();
        return checked(_allowedSet, _set, epochsNs);
    }

    @SafeVarargs // SaveVarargs because enum can't be extended
    public static <E extends Enum<E>> TypedStagedTimekeeper.Builder<E> builder(Class<E> clazz, E... allowed) {
        return new Builder<>(clazz, allowed);
    }

    protected TypedStagedTimekeeper(int allowedSet, int set, long[] epochsNs) {
        super(allowedSet, set, epochsNs);
    }

    // default time unit is nanoseconds
    public long getLoadTime(E stage) {
        return getLoadTime(NANOSECONDS, 1 << stage.ordinal());
    }

    public long getLoadTime(TimeUnit unit, E stage) {
        return getLoadTime(unit, 1 << stage.ordinal());
    }

    public long getLoadTime(Set<E> stages) {
        return getLoadTime(NANOSECONDS, stages);
    }

    public long getLoadTime(TimeUnit unit, Set<E> stages) {
        ensure(!stages.isEmpty(), () -> new IllegalArgumentException("No stages selected to query load time"));
        long sum = 0;
        for(E stage : stages) {
            if( ( allowedSet & (1 << stage.ordinal()) ) == 0) {
                throw new IllegalArgumentException(stage + " is not included in allowed stage set " + Integer.toBinaryString(allowedSet));
            }
            sum += epochsNs[stage.ordinal()];
        }
        return unit.convert(sum, NANOSECONDS);
    }

    public long getLoadTime(TimeUnit unit, E... stages) {
        long sum = 0;
        for(E stage : stages) {
            if( (allowedSet & (1 << stage.ordinal()) ) == 0) {
                throw new IllegalArgumentException(stage + " is not included in allowed stage set " + Integer.toBinaryString(allowedSet));
            }
            sum += epochsNs[stage.ordinal()];
        }
        return unit.convert(sum, NANOSECONDS);
    }
    
    public static class Builder<E extends Enum<E>> extends StagedTimekeeper.Builder {

        protected final Class<E> clazz;
        protected Builder(Class<E> clazz, E... allowed) {
            super(0); // initialize, then update appropriately
            this.clazz = clazz;
            for(E e : allowed) {
                allowedSet |= 1 << e.ordinal();
            }
            this.set = 0;
            this.epochsNs = EMPTY;
        }

        public StagesOpResult tryAddStage(E stage) {
            return tryAddStage(1 << stage.ordinal());
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
            if(tryAddStages(stages) == EX_ALREADY_EXISTS) {
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
            return unit.convert(trackAndReset(stage), NANOSECONDS);
        }

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
