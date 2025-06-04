package org.minerift.ether.util.pair;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import java.lang.reflect.Array;
import java.util.function.IntFunction;

public class Pair<F, S> {

    protected F first;
    protected S second;

    public Pair() {
        this(null, null);
    }

    public Pair(F first, S second) {
        this.first = first;
        this.second = second;
    }

    public Pair(Object[] pair) {
        Preconditions.checkArgument(pair.length == 2, "Must only be 2 elements in pair!");
        this.first = (F) pair[0];
        this.second = (S) pair[1];
    }

    public F getFirst() {
        return first;
    }

    public S getSecond() {
        return second;
    }

    public Pair.Mutable<F, S> asMutable() {
        return this instanceof Pair.Mutable<F,S> mut ? mut : new Mutable<>(first, second);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pair<?, ?> pair)) return false;
        return Objects.equal(first, pair.first) && Objects.equal(second, pair.second);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(first, second);
    }

    @Override
    public String toString() {
        return "Pair{" +
                "first=" + first +
                ", second=" + second +
                '}';
    }

    public static class Mutable<F, S> extends Pair<F, S> {

        public Mutable(Pair<F, S> pair) {
            this(pair.first, pair.second);
        }

        public Mutable() {
            super();
        }

        public Mutable(F first, S second) {
            super(first, second);
        }

        public Mutable(Object[] pair) {
            super(pair);
        }

        public void setFirst(F first) {
            this.first = first;
        }

        public void setSecond(S second) {
            this.second = second;
        }

        public Pair<F, S> copyAsImmutable() {
            return new Pair<>(first, second);
        }
    }

    // TODO: review; not sure I need this class at all
    @Deprecated
    public static class SameType<T> extends Pair<T, T> {
        public SameType(T first, T second) {
            super(first, second);
        }

        public SameType(T[] pair) {
            super(pair);
        }

        private T[] fillArray(T[] emptyArray) {
            emptyArray[0] = first;
            emptyArray[1] = second;
            return emptyArray;
        }

        public T[] toArray(Class<T> clazz) {
            return fillArray((T[]) Array.newInstance(clazz, 2));
        }

        // Preferred method override
        public T[] toArray(IntFunction<T[]> arrayCreator) {
            return fillArray(arrayCreator.apply(2));
        }
    }
}
