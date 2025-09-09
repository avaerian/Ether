package org.minerift.ether.util.nbt.tags.array;

import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.bytes.ByteList;
import org.minerift.ether.util.nbt.NbtTraverser;
import org.minerift.ether.util.nbt.TagCodec;
import org.minerift.ether.util.nbt.TagVisitor;
import org.minerift.ether.util.nbt.snbt.Snbt;
import org.minerift.ether.util.nbt.snbt.UnexpectedTokenException;
import org.minerift.ether.util.nbt.tags.TagType;
import org.minerift.ether.util.nbt.tags.TagTypes;

public final class ByteArrayTag extends ArrayTag<byte[]> {
    public ByteArrayTag(String name, byte[] value) {
        super(name, value);
    }

    public ByteArrayTag(String name) {
        super(name, new byte[0]);
    }

    @Override
    public void accept(TagVisitor visit) {
        visit.visitByteArray(this);
    }

    @Override
    public TagType<ByteArrayTag> getType() {
        return TagTypes.BYTE_ARRAY;
    }

    @Override
    public ByteArrayTag copy() {
        return new ByteArrayTag(name, value);
    }

    @Override
    public ByteArrayTag copy(boolean copyArray) {
        return new ByteArrayTag(name, copyArray ? value.clone() : value);
    }

    public static class Codec implements TagCodec<ByteArrayTag> {
        @Override
        public ByteArrayTag readTag(NbtTraverser nbt, String name) {
            int len = nbt.readInt();
            byte[] bytes = nbt.read(len);
            return new ByteArrayTag(name, bytes);
        }

        @Override
        public ByteArrayTag readTag(Snbt.Parser snbt, String name) throws UnexpectedTokenException {
            snbt.nextIf("[");
            if(snbt.nextIf("]")) {
                return new ByteArrayTag(name, new byte[0]);
            }

            ByteList bytes = new ByteArrayList();
            do {
                bytes.add(snbt.expectByte());
                if(snbt.nextIf("]")) {
                    break;
                }
            } while(snbt.nextIf(","));
            return new ByteArrayTag(name, bytes.toByteArray());
        }

        @Override
        public void writeTag(NbtTraverser nbt, ByteArrayTag tag) {
            nbt.writeInt(tag.getValue().length);
            nbt.write(tag.getValue());
        }

        @Override
        public int skip(NbtTraverser nbt) {
            int len = nbt.readInt();
            nbt.skip(len);
            return Integer.BYTES + len;
        }
    }
}
