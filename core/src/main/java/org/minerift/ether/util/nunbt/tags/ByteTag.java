package org.minerift.ether.util.nunbt.tags;

import org.minerift.ether.util.nunbt.NbtTraverser;
import org.minerift.ether.util.nunbt.TagCodec;
import org.minerift.ether.util.nunbt.snbt.Snbt;
import org.minerift.ether.util.nunbt.snbt.UnexpectedTokenException;

public class ByteTag extends Tag<Byte> {

    private byte value;

    public ByteTag(String name, byte value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public TagType getType() {
        return TagType.BYTE;
    }

    @Override
    public Byte getValue() {
        return getByteValue();
    }

    public byte getByteValue() {
        return value;
    }

    @Override
    public void setValue(Byte value) {
        setValue(value.byteValue());
    }

    @Override
    public ByteTag copy() {
        return new ByteTag(name, value);
    }

    public void setValue(byte value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "ByteTag{" +
                "value=" + value +
                ", name='" + name + '\'' +
                '}';
    }

    public static class Codec implements TagCodec<ByteTag> {
        @Override
        public ByteTag readTag(NbtTraverser nbt, String name) {
            return new ByteTag(name, nbt.readByte());
        }

        @Override
        public ByteTag readTag(Snbt.Parser snbt, String name) throws UnexpectedTokenException {
            byte b = snbt.expectByte();
            return new ByteTag(name, b);
        }

        @Override
        public void writeTag(NbtTraverser nbt, ByteTag tag) {
            nbt.writeByte(tag.getByteValue());
        }

        @Override
        public int skip(NbtTraverser nbt) {
            nbt.skip(Byte.BYTES);
            return Byte.BYTES;
        }
    }
}
