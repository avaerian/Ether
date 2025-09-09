package org.minerift.ether.util.nbt.tags;

import org.minerift.ether.util.nbt.TagVisitor;

public abstract class Tag {

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

    // implemented to distinct between the tag name and type name
    public String getTypeName() {
        return getType().getName();
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean is(TagType<?> type) {
        return getType().getTagClass().equals(type.getTagClass());
    }

    public abstract void accept(TagVisitor visit);

    public abstract TagType<?> getType();

    public boolean isPrimitiveType() {
        return false;
    }

    public abstract Tag copy();
}
