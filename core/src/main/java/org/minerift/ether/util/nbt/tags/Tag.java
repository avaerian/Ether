package org.minerift.ether.util.nbt.tags;

import org.minerift.ether.util.nbt.tags.container.MismatchedTypeException;
import org.minerift.ether.util.nbt.transmute.NbtTransmuteException;

import java.util.function.Function;
import java.util.function.Supplier;

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

    public <T extends Tag> T as(TagType<T> type) throws MismatchedTypeException {
        if(!is(type))
            throw new MismatchedTypeException("Type " + type + " does not match this tag type " + type());

        return (T) this;
    }

    public <T extends Tag, E extends Exception> T as(TagType<T> type, Function<MismatchedTypeException, E> ex) throws E {
        try {
            return as(type);
        } catch (MismatchedTypeException e) {
            throw ex.apply(e);
        }
    }
}
