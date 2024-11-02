package org.minerift.ether.database.bin;

public class Ref<T> {

    public static <T> Ref<T> ref(T val) {
        return new Ref<>(val);
    }

    private T val;
    public Ref(T val) {
        this.val = val;
    }

    public Ref() {
        this(null);
    }

    public void set(T newVal) {
        this.val = newVal;
    }

    public T get() {
        return val;
    }

    @Override
    public String toString() {
        return "Ref{" +
                "val=" + val +
                '}';
    }
}
