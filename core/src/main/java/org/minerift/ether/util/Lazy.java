package org.minerift.ether.util;

import lombok.Getter;

import java.util.function.Supplier;

public class Lazy<T> implements Supplier<T> {

    public static <T> Lazy<T> of(Supplier<T> supplier) {
        return new Lazy<>(supplier);
    }


    private final Supplier<T> supplier;
    private T obj;
    @Getter private boolean computed;

    public Lazy(Supplier<T> supplier) {
        this.supplier = supplier;
        this.obj = null;
        this.computed = false;
    }

    @Override
    public T get() {
        return computed
                ? obj
                : compute();
    }

    private T compute() {
        this.computed = true;
        return (obj = supplier.get());
    }
}
