package org.minerift.ether.util.nunbt.tags;

import org.minerift.ether.util.nunbt.NbtTraverser;
import org.minerift.ether.util.nunbt.TagCodec;
import org.minerift.ether.util.nunbt.snbt.Snbt;
import org.minerift.ether.util.nunbt.snbt.UnexpectedTokenException;

public class LongTag extends Tag<Long> {

    private long value;

    public LongTag(String name, long value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public TagType getType() {
        return TagType.LONG;
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
            nbt.writeLong(tag.getLongValue());
        }

        @Override
        public int skip(NbtTraverser nbt) {
            nbt.skip(Long.BYTES);
            return Long.BYTES;
        }
    }
}
