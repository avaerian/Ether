package org.minerift.ether.util.nbt.tags;

import org.minerift.ether.util.nbt.NbtTraverser;
import org.minerift.ether.util.nbt.TagCodec;
import org.minerift.ether.util.nbt.snbt.Snbt;
import org.minerift.ether.util.nbt.snbt.UnexpectedTokenException;

public class IntTag extends Tag implements ScalarTag {

    public static IntTag valueOf(int value) {
        return new IntTag("", value);
    }

    private int value;

    public IntTag(String name, int value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public TagType<IntTag> getType() {
        return TagType.INT;
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
        return value;
    }

    @Override
    public long getAsLong() {
        return value;
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

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public IntTag copy() {
        return new IntTag(name, value);
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
            nbt.writeInt(tag.getAsInt());
        }

        @Override
        public int skip(NbtTraverser nbt) {
            nbt.skip(Integer.BYTES);
            return Integer.BYTES;
        }
    }
}
