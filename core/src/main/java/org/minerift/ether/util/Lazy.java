package org.minerift.ether.util;

import java.util.function.Supplier;

public class Lazy<T> implements Supplier<T> {

    public static <T> Lazy<T> of(Supplier<T> supplier) {
        return new Lazy<>(supplier);
    }


    private final Supplier<T> supplier;
    private T obj;

    public Lazy(Supplier<T> supplier) {
        this.supplier = supplier;
        this.obj = null;
    }

    @Override
    public T get() {
        return obj == null ? (obj = supplier.get()) : obj;
    }
}
