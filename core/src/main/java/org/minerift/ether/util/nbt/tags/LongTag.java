package org.minerift.ether.util.nbt.tags;

import org.minerift.ether.util.nbt.NbtTraverser;
import org.minerift.ether.util.nbt.TagCodec;
import org.minerift.ether.util.nbt.snbt.Snbt;
import org.minerift.ether.util.nbt.snbt.UnexpectedTokenException;

public class LongTag extends Tag implements ScalarTag {

    public static LongTag valueOf(long value) {
        return new LongTag("", value);
    }

    private long value;

    public LongTag(String name, long value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public TagType<LongTag> getType() {
        return TagTypes.LONG;
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

    @Override
    public LongTag copy() {
        return new LongTag(name, value);
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

    public static class Codec implements TagCodec<LongTag> {

        @Override
        public LongTag readTag(NbtTraverser nbt, String name) {
            return new LongTag(name, nbt.readLong());
        }

        @Override
        public LongTag readTag(Snbt.Parser snbt, String name) throws UnexpectedTokenException {
            long l = snbt.expectLong();
            return new LongTag(name, l);
        }

        @Override
        public void writeTag(NbtTraverser nbt, LongTag tag) {
            nbt.writeLong(tag.getAsLong());
        }

        @Override
        public int skip(NbtTraverser nbt) {
            nbt.skip(Long.BYTES);
            return Long.BYTES;
        }
    }
}
