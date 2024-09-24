package org.minerift.ether.util.newnbt.tags;

import org.minerift.ether.util.newnbt.NBT;

public class FloatTag extends Tag<Float> {

    private float value;

    public FloatTag(String name, float value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public NBT.TagType getType() {
        return NBT.TagType.FLOAT_TAG;
    }

    @Override
    public Float getValue() {
        return getFloatValue();
    }

    public float getFloatValue() {
        return value;
    }

    @Override
    public void setValue(Float value) {
        setValue(value.floatValue());
    }

    public void setValue(float value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "FloatTag{" +
                "value=" + value +
                ", name='" + name + '\'' +
                '}';
    }
}
