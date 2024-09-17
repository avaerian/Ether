package org.minerift.ether.util.newnbt.tags;

import org.minerift.ether.util.newnbt.NBT;

public class EndTag extends Tag<Void> {

    public static final EndTag INSTANCE = new EndTag();

    private EndTag(String name) {
        super();
    }

    private EndTag() {
        super();
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public void setName(String name) {
        throw new UnsupportedOperationException("Unable to set name for NBT end tag!");
    }

    @Override
    public NBT.TagType getType() {
        return NBT.TagType.END_TAG;
    }

    @Override
    public Void getValue() {
        throw new UnsupportedOperationException("Unable to get value for NBT end tag!");
    }

    @Override
    public void setValue(Void value) {
        throw new UnsupportedOperationException("Unable to set value for NBT end tag!");
    }
}
