package org.minerift.ether.util.nbt.tags;

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

    public boolean hasName() {
        return !name.isBlank();
    }

    // implemented to distinct between the tag name and type name
    public String getTypeName() {
        return type().getName();
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean is(TagType<?> type) {
        return type().getTagClass().equals(type.getTagClass());
    }

    public abstract TagType<?> type();

    public boolean isPrimitiveType() {
        return false;
    }

    public abstract Tag copy();
}
