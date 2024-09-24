package org.minerift.ether.util.newnbt.tags.array;

import org.minerift.ether.util.newnbt.NBT;

public final class IntArrayTag extends ArrayTag<int[]> {
    public IntArrayTag(String name, int[] value) {
        super(name, value);
    }

    @Override
    public NBT.TagType getType() {
        return NBT.TagType.INT_ARRAY_TAG;
    }
}
