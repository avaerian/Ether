package org.minerift.ether.util.fn;

@FunctionalInterface
public interface IntBiConsumer<T> {
    void accept(int i, T t);
}
