package org.minerift.ether.util.newnbt.tags.array;

import org.minerift.ether.util.newnbt.NBT;

public final class LongArrayTag extends ArrayTag<long[]> {
    public LongArrayTag(String name, long[] value) {
        super(name, value);
    }

    @Override
    public NBT.TagType getType() {
        return NBT.TagType.LONG_ARRAY_TAG;
    }
}
