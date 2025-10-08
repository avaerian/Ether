package org.minerift.ether.util.nbt.tags.array;

import org.minerift.ether.util.nbt.tags.Tag;

// T is array
public abstract class ArrayTag<T> extends Tag {

    protected T value;

    public ArrayTag(String name, T value) {
        this.name = name;
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public abstract Tag copy(boolean copyArray);

    @Override
    public String toString() {
        return type().getName() + "{" +
                "value=" + value +
                ", name='" + name + '\'' +
                '}';
    }
}
