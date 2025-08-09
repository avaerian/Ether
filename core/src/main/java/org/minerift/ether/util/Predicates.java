package org.minerift.ether.util;

import java.util.function.Predicate;

public class Predicates {
    private static final Predicate<?> ALWAYS = (ignore) -> true;
    private static final Predicate<?> NEVER = (ignore) -> false;

    public static <E> Predicate<E> always() {
        return (Predicate<E>) ALWAYS;
    }

    public static <E> Predicate<E> never() {
        return (Predicate<E>) NEVER;
    }
}
