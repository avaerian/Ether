package org.minerift.ether.util.nunbt.tags;

import org.minerift.ether.util.nunbt.NbtTraverser;
import org.minerift.ether.util.nunbt.TagCodec;
import org.minerift.ether.util.nunbt.snbt.Snbt;
import org.minerift.ether.util.nunbt.snbt.UnexpectedTokenException;

public class IntTag extends Tag<Integer> {

    private int value;

    public IntTag(String name, int value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public TagType getType() {
        return TagType.INT;
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

    @Override
    public IntTag copy() {
        return new IntTag(name, value);
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

    public static class Codec implements TagCodec<IntTag> {
        @Override
        public IntTag readTag(NbtTraverser nbt, String name) {
            return new IntTag(name, nbt.readInt());
        }

        @Override
        public IntTag readTag(Snbt.Parser snbt, String name) throws UnexpectedTokenException {
            int i = snbt.expectInt();
            return new IntTag(name, i);
        }

        @Override
        public void writeTag(NbtTraverser nbt, IntTag tag) {
            nbt.writeInt(tag.getIntValue());
        }

        @Override
        public int skip(NbtTraverser nbt) {
            nbt.skip(Integer.BYTES);
            return Integer.BYTES;
        }
    }
}
