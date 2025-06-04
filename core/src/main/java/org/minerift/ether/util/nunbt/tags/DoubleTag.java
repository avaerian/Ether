package org.minerift.ether.util.nunbt.tags;

import org.minerift.ether.util.nunbt.NbtTraverser;
import org.minerift.ether.util.nunbt.TagCodec;
import org.minerift.ether.util.nunbt.snbt.Snbt;
import org.minerift.ether.util.nunbt.snbt.UnexpectedTokenException;

public class DoubleTag extends Tag<Double> {

    private double value;

    public DoubleTag(String name, double value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public TagType getType() {
        return TagType.DOUBLE;
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

    @Override
    public DoubleTag copy() {
        return new DoubleTag(name, value);
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

    public static class Codec implements TagCodec<DoubleTag> {
        @Override
        public DoubleTag readTag(NbtTraverser nbt, String name) {
            return new DoubleTag(name, nbt.buffer.getDouble());
        }

        @Override
        public DoubleTag readTag(Snbt.Parser snbt, String name) throws UnexpectedTokenException {
            double d = snbt.expectDouble();
            return new DoubleTag(name, d);
        }

        @Override
        public void writeTag(NbtTraverser nbt, DoubleTag tag) {
            nbt.writeDouble(tag.getDoubleValue());
        }

        @Override
        public int skip(NbtTraverser nbt) {
            nbt.skip(Double.BYTES);
            return Double.BYTES;
        }
    }
}
