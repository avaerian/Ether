package org.minerift.ether.util.newnbt.tags;

import org.minerift.ether.util.newnbt.NBT;

public class ByteTag extends Tag<Byte> {

    private byte value;

    public ByteTag(String name, byte value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public NBT.TagType getType() {
        return NBT.TagType.BYTE_TAG;
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
}
