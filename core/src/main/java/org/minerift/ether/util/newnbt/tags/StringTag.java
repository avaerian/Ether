package org.minerift.ether.util.newnbt.tags;

import org.minerift.ether.util.newnbt.NBT;

public class StringTag extends Tag<String> {

    private String value;

    public StringTag(String name, String value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public NBT.TagType getType() {
        return NBT.TagType.STRING_TAG;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "StringTag{" +
                "value='" + value + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
