package org.minerift.ether.util.nbt.tags;

import org.minerift.ether.util.nbt.NbtTraverser;
import org.minerift.ether.util.nbt.TagCodec;
import org.minerift.ether.util.nbt.snbt.Snbt;
import org.minerift.ether.util.nbt.snbt.UnexpectedTokenException;

public class FloatTag extends Tag implements ScalarTag {

    public static FloatTag valueOf(float value) {
        return new FloatTag("", value);
    }

    private float value;

    public FloatTag(String name, float value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public TagType<FloatTag> type() {
        return TagTypes.FLOAT;
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
        return value;
    }

    @Override
    public double getAsDouble() {
        return value;
    }

    @Override
    public Number getAsNumber() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    @Override
    public FloatTag copy() {
        return new FloatTag(name, value);
    }

    @Override
    public String toString() {
        return "FloatTag{" +
                "value=" + value +
                ", name='" + name + '\'' +
                '}';
    }

    public static class Codec implements TagCodec<FloatTag> {
        @Override
        public FloatTag readTag(NbtTraverser nbt, String name) {
            return new FloatTag(name, nbt.readFloat());
        }

        @Override
        public FloatTag readTag(Snbt.Parser snbt, String name) throws UnexpectedTokenException {
            float f = snbt.expectFloat();
            return new FloatTag(name, f);
        }

        @Override
        public void writeTag(NbtTraverser nbt, FloatTag tag) {
            nbt.writeFloat(tag.getAsFloat());
        }

        @Override
        public void writeTag(StringBuilder str, FloatTag tag) {
            str.append(tag.getAsFloat());
            str.append('f');
        }

        @Override
        public int skip(NbtTraverser nbt) {
            nbt.skip(Float.BYTES);
            return Float.BYTES;
        }
    }
}
