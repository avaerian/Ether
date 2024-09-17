package org.minerift.ether.util.newnbt.tags;

import org.minerift.ether.util.newnbt.NBT;

public class IntTag extends Tag<Integer> {

    private int value;

    public IntTag(String name, int value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public NBT.TagType getType() {
        return NBT.TagType.INT_TAG;
    }

    @Override
    public Integer getValue() {
        return getIntValue();
    }

    public int getIntValue() {
        return value;
    }

    @Override
    public void setValue(Integer value) {
        setValue(value.intValue());
    }

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "IntTag{" +
                "value=" + value +
                ", name='" + name + '\'' +
                '}';
    }
}
