package org.minerift.ether.util.newnbt.tags;

import org.minerift.ether.util.newnbt.NBT;

public abstract class Tag<V> {

    protected String name;

    public Tag(String name) {
        this.name = name;
    }

    public Tag() {
        this("");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public abstract NBT.TagType getType();

    public abstract V getValue();
    public abstract void setValue(V value);

}
