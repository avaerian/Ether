package org.minerift.ether.util.pair;

import java.lang.reflect.Array;
import java.util.function.IntFunction;

public class SameTypePair<T> extends Pair<T, T> {

    public SameTypePair(T first, T second) {
        super(first, second);
    }

    public SameTypePair(T[] pair) {
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
