package org.minerift.ether.util;

public sealed class Option<T> permits Option.Some, Option.None {

    public static <T> Some<T> some(T val) {
        return new Some<>(val);
    }

    public static <T> None<T> none() {
        return (None<T>) None.NONE;
    }

    public static final class Some<T> extends Option<T> {
        private final T val;
        public Some(T val) {
            this.val = val;
        }

        public T value() {
            return val;
        }

        @Override
        public String toString() {
            return "Some(" + val + ")";
        }
    }

    public static final class None<T> extends Option<T> {
        public static final None NONE = new None();
        private None() {
            // empty
        }

        @Override
        public String toString() {
            return "None";
        }
    }
}
