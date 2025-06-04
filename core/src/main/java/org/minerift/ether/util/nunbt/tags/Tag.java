package org.minerift.ether.util.nunbt.tags;

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

    public boolean is(TagType type) {
        return type == getType();
    }

    public abstract TagType getType();

    public abstract V getValue();
    public abstract void setValue(V value);

    // TODO
    public abstract Tag<V> copy();

}
