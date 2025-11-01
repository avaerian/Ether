package org.minerift.ether.util;

public class TypedStagedTimekeeper<E extends Enum<E>>
        extends StagedBuilder {

        protected final Class<E> clazz;
        public TypedStagedTimekeeper(Stopwatch timer, Class<E> clazz) {
            this(timer, clazz.getEnumConstants());
        }

        public TypedStagedTimekeeper(Stopwatch timer, Class<E> clazz, E ... allowed) {
            if(allowed.length > StagedTimekeeper.getStageCount()) {
                // TODO: review
            }
            for(E e : allowed) {
                
            }
        }

        // exists for sake of not supporting empty 
        @Deprecated // TODO: review
        public TypedStagedTimekeeper(Stopwatch timer, E allowed) {
            throw new UnreachableException("unimplented");
        }
}