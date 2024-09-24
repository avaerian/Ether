package org.minerift.ether.util.newnbt.tags;

import org.minerift.ether.util.newnbt.NBT;

public class DoubleTag extends Tag<Double> {

    private double value;

    public DoubleTag(String name, double value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public NBT.TagType getType() {
        return NBT.TagType.DOUBLE_TAG;
    }

    @Override
    public Double getValue() {
        return getDoubleValue();
    }

    public double getDoubleValue() {
        return value;
    }

    @Override
    public void setValue(Double value) {
        setValue(value.doubleValue());
    }

    public void setValue(double value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "DoubleTag{" +
                "value=" + value +
                ", name='" + name + '\'' +
                '}';
    }
}
