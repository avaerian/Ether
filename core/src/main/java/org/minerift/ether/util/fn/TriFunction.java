package org.minerift.ether.util.fn;

import java.util.Objects;

// A, B, C - parameters
// R - result
@FunctionalInterface
public interface TriFunction<A, B, C, R> {
    R accept(A a, B b, C c);

    default TriFunction<A, B, C, R> andThen(TriFunction<? super A, ? super B, ? super C, ? super R> after) {
        Objects.requireNonNull(after);
        return (A a, B b, C c) -> { accept(a, b, c); return (R) after.accept(a, b, c); };
    }
}
