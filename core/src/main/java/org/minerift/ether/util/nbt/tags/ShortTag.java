package org.minerift.ether.util.nbt.tags;

import org.minerift.ether.util.nbt.NbtTraverser;
import org.minerift.ether.util.nbt.TagCodec;
import org.minerift.ether.util.nbt.TagVisitor;
import org.minerift.ether.util.nbt.snbt.Snbt;
import org.minerift.ether.util.nbt.snbt.UnexpectedTokenException;

public class ShortTag extends Tag implements ScalarTag {

    public static ShortTag valueOf(short value) {
        return new ShortTag("", value);
    }

    private short value;

    public ShortTag(String name, short value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public void accept(TagVisitor visit) {
        visit.visitShort(this);
    }

    @Override
    public TagType<ShortTag> type() {
        return TagTypes.SHORT;
    }


    public void setValue(short value) {
        this.value = value;
    }

    @Override
    public byte getAsByte() {
        return (byte) value;
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

    @Override
    public ShortTag copy() {
        return new ShortTag(name, value);
    }

    @Override
    public String toString() {
        return "ShortTag{" +
                "value=" + value +
                ", name='" + name + '\'' +
                '}';
    }

    public static class Codec implements TagCodec<ShortTag> {
        @Override
        public void writeTag(NbtTraverser nbt, ShortTag tag) {
            nbt.writeShort(tag.getAsShort());
        }

        @Override
        public void writeTag(StringBuilder str, ShortTag tag) {
            str.append(tag.getAsShort());
            str.append('s');
        }

        @Override
        public ShortTag readTag(NbtTraverser nbt, String name) {
            return new ShortTag(name, nbt.readShort());
        }

        @Override
        public ShortTag readTag(Snbt.Parser snbt, String name) throws UnexpectedTokenException {
            short s = snbt.expectShort();
            return new ShortTag(name, s);
        }

        @Override
        public int skip(NbtTraverser nbt) {
            nbt.skip(Short.BYTES);
            return Short.BYTES;
        }
    }
}
