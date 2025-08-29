package org.minerift.ether.util.nbt.tags;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import org.minerift.ether.util.nbt.TagCodec;
import org.minerift.ether.util.nbt.tags.array.ArrayTag;
import org.minerift.ether.util.nbt.tags.container.AbstractContainerTag;

public class TagType<T extends Tag> {

    private final PrimitiveTagType type;
    private final Class<? extends T> tagClazz;

    private final Supplier<TagCodec<T>> codec;

    public TagType(PrimitiveTagType type, Class<? extends T> tagClazz, Supplier<TagCodec<T>> codec) {
        this.type = type;
        this.tagClazz = tagClazz;
        this.codec = Suppliers.memoize(codec);
    }

    // get the primitive type that a [custom] tag represents
    public PrimitiveTagType getPrimitiveType() {
        return type;
    }

    public Class<? extends T> getTagClass() {
        return tagClazz;
    }

    public TagCodec<T> codec() {
        return codec.get();
    }

    public byte getId() {
        return type.getId();
    }

    public String getName() {
        return "TAG_" + getPrimitiveType().name();
    }

    public boolean isArray() {
        return ArrayTag.class.isAssignableFrom(tagClazz);
    }

    public boolean isContainer() {
        return AbstractContainerTag.class.isAssignableFrom(tagClazz);
    }
}
