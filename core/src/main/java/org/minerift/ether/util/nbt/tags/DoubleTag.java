package org.minerift.ether.util.nbt.tags;

import org.minerift.ether.util.nbt.NbtTraverser;
import org.minerift.ether.util.nbt.TagCodec;
import org.minerift.ether.util.nbt.snbt.Snbt;
import org.minerift.ether.util.nbt.snbt.UnexpectedTokenException;

public class DoubleTag extends Tag implements ScalarTag {

    public static DoubleTag valueOf(double value) {
        return new DoubleTag("", value);
    }

    private double value;

    public DoubleTag(String name, double value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public TagType<DoubleTag> getType() {
        return TagTypes.DOUBLE;
    }

    @Override
    public byte getAsByte() {
        return (byte) value;
    }

    @Override
    public short getAsShort() {
        return (short) value;
    }

    @Override
    public int getAsInt() {
        return (int) value;
    }

    @Override
    public long getAsLong() {
        return (long) value;
    }

    @Override
    public float getAsFloat() {
        return (float) value;
    }

    @Override
    public double getAsDouble() {
        return value;
    }

    @Override
    public Number getAsNumber() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    @Override
    public DoubleTag copy() {
        return new DoubleTag(name, value);
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
            nbt.writeDouble(tag.getAsDouble());
        }

        @Override
        public int skip(NbtTraverser nbt) {
            nbt.skip(Double.BYTES);
            return Double.BYTES;
        }
    }
}
