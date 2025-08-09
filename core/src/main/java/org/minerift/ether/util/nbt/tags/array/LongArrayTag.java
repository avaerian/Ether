package org.minerift.ether.util.nbt.tags.array;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import org.minerift.ether.util.nbt.NbtTraverser;
import org.minerift.ether.util.nbt.TagCodec;
import org.minerift.ether.util.nbt.snbt.Snbt;
import org.minerift.ether.util.nbt.snbt.UnexpectedTokenException;
import org.minerift.ether.util.nbt.tags.PrimitiveTagType;
import org.minerift.ether.util.nbt.tags.TagType;

public final class LongArrayTag extends ArrayTag<long[]> {
    public LongArrayTag(String name, long[] value) {
        super(name, value);
    }

    @Override
    public TagType<LongArrayTag> getType() {
        return TagType.LONG_ARRAY;
    }

    @Override
    public LongArrayTag copy() {
        return new LongArrayTag(name, value);
    }

    @Override
    public LongArrayTag copy(boolean copyArray) {
        return new LongArrayTag(name, copyArray ? value.clone() : value);
    }

    public static class Codec implements TagCodec<LongArrayTag> {
        @Override
        public LongArrayTag readTag(NbtTraverser nbt, String name) {
            int len = nbt.readInt();
            long[] longs = new long[len];
            nbt.buffer.asLongBuffer().get(longs); // increments cursor for new buffer, but not for original
            nbt.skip(Long.BYTES * len); // move forward in main buffer
            return new LongArrayTag(name, longs);
        }

        @Override
        public LongArrayTag readTag(Snbt.Parser snbt, String name) throws UnexpectedTokenException {
            snbt.nextIf("[");
            if(snbt.nextIf("]")) {
                return new LongArrayTag(name, new long[0]);
            }

            LongList longs = new LongArrayList();
            do {
                longs.add(snbt.expectLong());
                if(snbt.nextIf("]")) {
                    break;
                }
            } while(snbt.nextIf(","));
            return new LongArrayTag(name, longs.toLongArray());
        }

        @Override
        public void writeTag(NbtTraverser nbt, LongArrayTag tag) {
            nbt.writeInt(tag.getValue().length);
            nbt.writeLongArray(tag.getValue());
        }

        @Override
        public int skip(NbtTraverser nbt) {
            int len = nbt.readInt();
            nbt.skip(Long.BYTES * len);
            return Integer.BYTES + (Long.BYTES * len);
        }
    }
}
