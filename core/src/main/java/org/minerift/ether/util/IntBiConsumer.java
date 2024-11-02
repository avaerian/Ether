package org.minerift.ether.util;

@FunctionalInterface
public interface IntBiConsumer<T> {
    void accept(int i, T t);
}
