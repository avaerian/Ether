package org.minerift.ether.util.newnbt.tags.array;

import org.minerift.ether.util.newnbt.tags.Tag;

public sealed abstract class ArrayTag<T> extends Tag<T> permits ByteArrayTag, IntArrayTag, LongArrayTag {

    protected T value;

    public ArrayTag(String name, T value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public T getValue() {
        return value;
    }

    @Override
    public void setValue(T value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return getType().getName() + "{" +
                "value=" + value +
                ", name='" + name + '\'' +
                '}';
    }
}
