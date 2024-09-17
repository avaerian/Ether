package org.minerift.ether.util.newnbt.tags;

import org.minerift.ether.util.newnbt.NBT;

public class LongTag extends Tag<Long> {

    private long value;

    public LongTag(String name, long value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public NBT.TagType getType() {
        return NBT.TagType.LONG_TAG;
    }

    @Override
    public Long getValue() {
        return getLongValue();
    }

    public long getLongValue() {
        return value;
    }

    @Override
    public void setValue(Long value) {
        setValue(value.longValue());
    }

    public void setValue(long value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "LongTag{" +
                "value=" + value +
                ", name='" + name + '\'' +
                '}';
    }
}
