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

    public void setName(String name) {
        this.name = name;
    }

    @Deprecated
    public boolean is(PrimitiveTagType type) {
        return type == getPrimitiveType();
    }

    public boolean is(TagType<?> type) {
        return getType().getTagClass().equals(type.getTagClass());
    }

    public abstract TagType<?> getType();

    public boolean isPrimitiveType() {
        return false;
    }

    public PrimitiveTagType getPrimitiveType() {
        return getType().getPrimitiveType();
    }

    public abstract Tag copy();
}
