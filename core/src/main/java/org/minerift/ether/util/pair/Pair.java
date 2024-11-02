package org.minerift.ether.util.pair;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import java.lang.reflect.Array;
import java.util.function.IntFunction;

public class Pair<F, S> {

    protected final F first;
    protected final S second;

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
