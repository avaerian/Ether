package org.minerift.ether.util.nbt.tags.array;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntIterators;
import it.unimi.dsi.fastutil.ints.IntList;
import org.minerift.ether.util.iter.ByteIterator;
import org.minerift.ether.util.nbt.NbtTraverser;
import org.minerift.ether.util.nbt.TagCodec;
import org.minerift.ether.util.nbt.TagVisitor;
import org.minerift.ether.util.nbt.snbt.Snbt;
import org.minerift.ether.util.nbt.snbt.UnexpectedTokenException;
import org.minerift.ether.util.nbt.tags.TagType;
import org.minerift.ether.util.nbt.tags.TagTypes;

import java.util.Arrays;

public final class IntArrayTag extends ArrayTag<int[]> {
    public static IntArrayTag valueOf(int[] value) {
        return new IntArrayTag("", value);
    }

    public IntArrayTag(String name, int[] value) {
        super(name, value);
    }

    @Override
    public void accept(TagVisitor visit) {
        visit.visitIntArray(this);
    }

    @Override
    public TagType<IntArrayTag> type() {
        return TagTypes.INT_ARRAY;
    }

    @Override
    public IntArrayTag copy() {
        return new IntArrayTag(name, value);
    }

    @Override
    public IntArrayTag copy(boolean copyArray) {
        return new IntArrayTag(name, copyArray ? value.clone() : value);
    }

    @Override
    public String toString() {
        return "IntArrayTag{" +
                "value=" + Arrays.toString(value) +
                ", name='" + name + '\'' +
                '}';
    }

    public static class Codec implements TagCodec<IntArrayTag> {
        @Override
        public IntArrayTag readTag(NbtTraverser nbt, String name) {
            int len = nbt.readInt();
            int[] ints = new int[len];
            nbt.readInts(ints);
            return new IntArrayTag(name, ints);
        }

        @Override
        public IntArrayTag readTag(Snbt.Parser snbt, String name) throws UnexpectedTokenException {
            snbt.nextIf("[");
            if(snbt.nextIf("]")) {
                return new IntArrayTag(name, new int[0]);
            }

            IntList ints = new IntArrayList();
            do {
                ints.add(snbt.expectInt());
                if(snbt.nextIf("]")) {
                    break;
                }
            } while(snbt.nextIf(","));
            return new IntArrayTag(name, ints.toIntArray());
        }

        @Override
        public void writeTag(NbtTraverser nbt, IntArrayTag tag) {
            nbt.writeInt(tag.getValue().length);
            nbt.writeIntArray(tag.getValue());
        }

        @Override
        public void writeTag(StringBuilder str, IntArrayTag tag) {
            str.append("[I");
            IntIterator it = IntIterators.wrap(tag.getValue());
            while(it.hasNext()) {
                int i = it.nextInt();
                str.append(i);

                if(it.hasNext()) {
                    str.append(",");
                }
            }
            str.append(']');
        }

        @Override
        public int skip(NbtTraverser nbt) {
            int len = nbt.readInt();
            nbt.skip(Integer.BYTES * len);
            return Integer.BYTES + (Integer.BYTES * len);
        }
    }
}
