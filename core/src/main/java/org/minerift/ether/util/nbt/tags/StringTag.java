package org.minerift.ether.util.nbt.tags;

import org.minerift.ether.util.nbt.NbtTraverser;
import org.minerift.ether.util.nbt.TagCodec;
import org.minerift.ether.util.nbt.snbt.Snbt;
import org.minerift.ether.util.nbt.snbt.UnexpectedTokenException;

public class StringTag extends Tag {

    public static StringTag valueOf(String value) {
        return new StringTag("", value);
    }

    private String value;

    public StringTag(String name, String value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public TagType<StringTag> getType() {
        return TagType.STRING;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public StringTag copy() {
        return new StringTag(name, value);
    }

    @Override
    public String toString() {
        return "StringTag{" +
                "value='" + value + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

    public static class Codec implements TagCodec<StringTag> {
        @Override
        public StringTag readTag(Snbt.Parser snbt, String name) throws UnexpectedTokenException {
            return new StringTag(name, snbt.expectUTF8());
        }

        @Override
        public void writeTag(NbtTraverser nbt, StringTag tag) {
            nbt.writeUTF8(tag.getValue());
        }

        @Override
        public StringTag readTag(NbtTraverser nbt, String name) {
            return new StringTag(name, nbt.readUTF8());
        }

        @Override
        public int skip(NbtTraverser nbt) {
            short len = (short) (nbt.readShort() & 0xFF);
            nbt.skip(len);
            return Short.BYTES + len;
        }
    }
}
