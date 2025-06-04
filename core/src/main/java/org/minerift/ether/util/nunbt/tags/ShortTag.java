package org.minerift.ether.util.nunbt.tags;

import org.minerift.ether.util.nunbt.NbtTraverser;
import org.minerift.ether.util.nunbt.TagCodec;
import org.minerift.ether.util.nunbt.snbt.Snbt;
import org.minerift.ether.util.nunbt.snbt.UnexpectedTokenException;

public class ShortTag extends Tag<Short> {

    private short value;

    public ShortTag(String name, short value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public TagType getType() {
        return TagType.SHORT;
    }

    @Override
    public Short getValue() {
        return getShortValue();
    }

    public short getShortValue() {
        return value;
    }

    @Override
    public void setValue(Short value) {
        setValue(value.shortValue());
    }

    @Override
    public ShortTag copy() {
        return new ShortTag(name, value);
    }

    public void setValue(short value) {
        this.value = value;
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
            nbt.writeShort(tag.getShortValue());
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
