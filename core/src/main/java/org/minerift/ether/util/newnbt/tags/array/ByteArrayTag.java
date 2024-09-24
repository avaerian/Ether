package org.minerift.ether.util.newnbt.tags.array;

import org.minerift.ether.util.newnbt.NBT;

public final class ByteArrayTag extends ArrayTag<byte[]> {
    public ByteArrayTag(String name, byte[] value) {
        super(name, value);
    }

    @Override
    public NBT.TagType getType() {
        return NBT.TagType.BYTE_ARRAY_TAG;
    }
}
