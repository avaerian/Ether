package org.minerift.ether.util.nunbt.tags;

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import org.minerift.ether.util.nunbt.NbtTraverser;
import org.minerift.ether.util.nunbt.TagCodec;
import org.minerift.ether.util.nunbt.tags.array.ArrayTag;
import org.minerift.ether.util.nunbt.tags.array.ByteArrayTag;
import org.minerift.ether.util.nunbt.tags.array.IntArrayTag;
import org.minerift.ether.util.nunbt.tags.array.LongArrayTag;
import org.minerift.ether.util.nunbt.tags.container.AbstractContainerTag;
import org.minerift.ether.util.nunbt.tags.container.CompoundTag;
import org.minerift.ether.util.nunbt.tags.container.ListTag;

public enum TagType {

    END(EndTag.class, EndTag.Codec::new),

    BYTE(ByteTag.class, ByteTag.Codec::new),

    SHORT(ShortTag.class, ShortTag.Codec::new),

    INT(IntTag.class, IntTag.Codec::new),

    LONG(LongTag.class, LongTag.Codec::new),

    FLOAT(FloatTag.class, FloatTag.Codec::new),

    DOUBLE(DoubleTag.class, DoubleTag.Codec::new),

    BYTE_ARRAY(ByteArrayTag.class, ByteArrayTag.Codec::new),

    STRING(StringTag.class, StringTag.Codec::new),

    LIST(ListTag.class, ListTag.Codec::new),

    // TODO: to allow for streaming, queue up tag read operations?

    COMPOUND(CompoundTag.class, CompoundTag.Codec::new),

    INT_ARRAY(IntArrayTag.class, IntArrayTag.Codec::new),
    LONG_ARRAY(LongArrayTag.class, LongArrayTag.Codec::new),

    ;

    public static final TagType[] VALUES = values();
    public static TagType lookup(byte id) {
        Preconditions.checkElementIndex(id, VALUES.length, "Unknown tag type " + id);
        return VALUES[id];
    }

    private final Class<? extends Tag> tagClazz;

    private final Supplier<TagCodec<?>> codec; // TODO: switch to regular init vs lazy loading

    TagType(Class<? extends Tag> tagClazz, Supplier<TagCodec<?>> codec) {
        this.tagClazz = tagClazz;
        this.codec = Suppliers.memoize(codec);
    }

    public String getName() {
        return "TAG_" + name();
    }

    public TagCodec<?> getCodec() {
        return codec.get();
    }

    public byte getId() {
        return (byte) ordinal();
    }

    public Class<? extends Tag> getTagClass() {
        return tagClazz;
    }

    public boolean isArray() {
        return ArrayTag.class.isAssignableFrom(tagClazz);
    }

    public boolean isContainer() {
        return AbstractContainerTag.class.isAssignableFrom(tagClazz);
    }

    public Tag<?> readTag(NbtTraverser nbt, String name) {
        return getCodec().readTag(nbt, name);
    }

    public void writeTag(NbtTraverser nbt, Tag<?> tag) {
        Preconditions.checkArgument(tag.getType() == this, "Unable to write tag " + tag.getType() + " as " + this);
        ((TagCodec<Tag<?>>)getCodec()).writeTag(nbt, tag);
    }

    public int skip(NbtTraverser nbt) {
        return getCodec().skip(nbt);
    }
}