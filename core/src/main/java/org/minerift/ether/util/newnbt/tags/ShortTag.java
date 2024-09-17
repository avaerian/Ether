package org.minerift.ether.util.newnbt.tags;

import org.minerift.ether.util.newnbt.NBT;

public class ShortTag extends Tag<Short> {

    private short value;

    public ShortTag(String name, short value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public NBT.TagType getType() {
        return NBT.TagType.SHORT_TAG;
    }

    @Override
    public Short getValue() {
        return getShortValue();
    }

    public short getShortValue() {
        return value;
    }

    @Override
    public void setValue(Short value) {
        setValue(value.shortValue());
    }

    public void setValue(short value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "ShortTag{" +
                "value=" + value +
                ", name='" + name + '\'' +
                '}';
    }
}
