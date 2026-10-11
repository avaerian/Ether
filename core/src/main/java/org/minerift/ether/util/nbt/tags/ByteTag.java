package org.minerift.ether.util.nbt.tags;

import org.minerift.ether.util.Note;
import org.minerift.ether.util.nbt.NbtTraverser;
import org.minerift.ether.util.nbt.TagCodec;
import org.minerift.ether.util.nbt.snbt.Snbt;
import org.minerift.ether.util.nbt.snbt.UnexpectedTokenException;

import java.util.Objects;

public class ByteTag extends Tag implements ScalarTag {

    public static ByteTag valueOf(byte value) {
        return new ByteTag("", value);
    }

    private byte value;

    public ByteTag(String name, byte value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public TagType<ByteTag> type() {
        return TagTypes.BYTE;
    }

    @Note("If 0, return false. For all other values, return true")
    public boolean getAsBoolean() {
        return value != 0;
    }

    @Override
    public byte getAsByte() {
        return value;
    }

    @Override
    public short getAsShort() {
        return value;
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

    public void setValue(byte value) {
        this.value = value;
    }

    @Override
    public ByteTag copy() {
        return new ByteTag(name, value);
    }

    @Override
    public String toString() {
        return "ByteTag{" +
                "value=" + value +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ByteTag byteTag)) return false;
        return value == byteTag.value;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
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
            nbt.writeByte(tag.getAsByte());
        }

        @Override
        public void writeTag(StringBuilder str, ByteTag tag) {
            str.append(tag.getAsByte());
            str.append('b');
        }

        @Override
        public int skip(NbtTraverser nbt) {
            nbt.skip(Byte.BYTES);
            return Byte.BYTES;
        }
    }
}
