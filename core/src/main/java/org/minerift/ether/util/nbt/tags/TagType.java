package org.minerift.ether.util.nbt.tags;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import org.minerift.ether.util.nbt.TagCodec;
import org.minerift.ether.util.nbt.tags.array.ArrayTag;
import org.minerift.ether.util.nbt.tags.container.AbstractContainerTag;

public class TagType<T extends Tag> {

    private final byte id;
    private final String name;
    private final Class<T> tagClazz;

    private final Supplier<TagCodec<T>> codec;

    public TagType(byte id, String name, Class<T> tagClazz, Supplier<TagCodec<T>> codec) {
        this.id = id;
        this.name = name;
        this.tagClazz = tagClazz;
        this.codec = Suppliers.memoize(codec);
    }

    public Class<? extends T> getTagClass() {
        return tagClazz;
    }

    public TagCodec<T> codec() {
        return codec.get();
    }

    public byte getId() {
        return id;
    }

    public String getName() {
        return "TAG_" + name;
    }

    public String getRawName() {
        return name;
    }

    public boolean isArray() {
        return ArrayTag.class.isAssignableFrom(tagClazz);
    }

    public boolean isContainer() {
        return AbstractContainerTag.class.isAssignableFrom(tagClazz);
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public int hashCode() {
        return id;
    }
}
