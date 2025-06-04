package org.minerift.ether.util.nunbt.tags;

import org.minerift.ether.util.nunbt.NbtTraverser;
import org.minerift.ether.util.nunbt.TagCodec;
import org.minerift.ether.util.nunbt.snbt.Snbt;
import org.minerift.ether.util.nunbt.snbt.UnexpectedTokenException;

public class FloatTag extends Tag<Float> {

    private float value;

    public FloatTag(String name, float value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public TagType getType() {
        return TagType.FLOAT;
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

    @Override
    public FloatTag copy() {
        return new FloatTag(name, value);
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

    public static class Codec implements TagCodec<FloatTag> {
        @Override
        public FloatTag readTag(NbtTraverser nbt, String name) {
            return new FloatTag(name, nbt.buffer.getFloat());
        }

        @Override
        public FloatTag readTag(Snbt.Parser snbt, String name) throws UnexpectedTokenException {
            float f = snbt.expectFloat();
            return new FloatTag(name, f);
        }

        @Override
        public void writeTag(NbtTraverser nbt, FloatTag tag) {
            nbt.writeFloat(tag.getFloatValue());
        }

        @Override
        public int skip(NbtTraverser nbt) {
            nbt.skip(Float.BYTES);
            return Float.BYTES;
        }
    }
}
