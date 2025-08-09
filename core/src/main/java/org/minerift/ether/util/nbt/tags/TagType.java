package org.minerift.ether.util.nbt.tags;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectArrayMap;
import org.minerift.ether.util.Lazy;
import org.minerift.ether.util.nbt.TagCodec;
import org.minerift.ether.util.nbt.tags.array.ArrayTag;
import org.minerift.ether.util.nbt.tags.array.ByteArrayTag;
import org.minerift.ether.util.nbt.tags.array.IntArrayTag;
import org.minerift.ether.util.nbt.tags.array.LongArrayTag;
import org.minerift.ether.util.nbt.tags.container.AbstractContainerTag;
import org.minerift.ether.util.nbt.tags.container.CompoundTag;
import org.minerift.ether.util.nbt.tags.container.ListTag;

import java.util.HashMap;
import java.util.Map;

public class TagType<T extends Tag> {

    public static final TagType<ByteTag> BYTE;
    public static final TagType<ShortTag> SHORT;
    public static final TagType<IntTag> INT;
    public static final TagType<LongTag> LONG;
    public static final TagType<FloatTag> FLOAT;
    public static final TagType<DoubleTag> DOUBLE;

    public static final TagType<StringTag> STRING;

    public static final TagType<ByteArrayTag> BYTE_ARRAY;
    public static final TagType<IntArrayTag> INT_ARRAY;
    public static final TagType<LongArrayTag> LONG_ARRAY;

    public static final TagType<ListTag> LIST;
    public static final TagType<CompoundTag> COMPOUND;

    public static final TagType<EndTag> END;

    static {
        BYTE = register(PrimitiveTagType.BYTE, ByteTag.class, ByteTag.Codec::new);
        SHORT = register(PrimitiveTagType.SHORT, ShortTag.class, ShortTag.Codec::new);
        INT = register(PrimitiveTagType.INT, IntTag.class, IntTag.Codec::new);
        LONG = register(PrimitiveTagType.LONG, LongTag.class, LongTag.Codec::new);
        FLOAT = register(PrimitiveTagType.FLOAT, FloatTag.class, FloatTag.Codec::new);
        DOUBLE = register(PrimitiveTagType.DOUBLE, DoubleTag.class, DoubleTag.Codec::new);

        STRING = register(PrimitiveTagType.STRING, StringTag.class, StringTag.Codec::new);

        BYTE_ARRAY  = register(PrimitiveTagType.BYTE_ARRAY, ByteArrayTag.class, ByteArrayTag.Codec::new);
        INT_ARRAY   = register(PrimitiveTagType.INT_ARRAY, IntArrayTag.class, IntArrayTag.Codec::new);
        LONG_ARRAY  = register(PrimitiveTagType.LONG_ARRAY, LongArrayTag.class, LongArrayTag.Codec::new);

        LIST = register(PrimitiveTagType.LIST, ListTag.class, ListTag.Codec::new);
        COMPOUND = register(PrimitiveTagType.COMPOUND, CompoundTag.class, CompoundTag.Codec::new);

        END = register(PrimitiveTagType.END, EndTag.class, EndTag.Codec::new);

    }

    public static <T extends Tag> TagType<T> register(PrimitiveTagType primType, Class<? extends T> tagClazz, Supplier<TagCodec<T>> codec) {
        return register(new TagType<>(primType, tagClazz, codec));
    }

    public static <T extends Tag> TagType<T> register(TagType<T> type) {
        if(CLASS_TO_TYPES.get().putIfAbsent(type.tagClazz, type) != null
            ^ ID_TO_TYPES.putIfAbsent(type.getId(), type) != null) {

            // TODO: throw exception??? -> mismatched registry; review this later
            throw new IllegalStateException("TagType registry seems mismatched");
        }
        return type;
    }

    public static TagType<?> lookup(byte id) {
        return ID_TO_TYPES.get(id);
    }

    public static TagType<?> lookup(PrimitiveTagType type) {
        return lookup(type.getId());
    }

    public static <T extends Tag> TagType<T> lookup(Class<? extends T> clazz) {
        TagType<?> type = CLASS_TO_TYPES.get().get(clazz);
        return (TagType<T>) type;
    }

    private static final Lazy<Map<Class<? extends Tag>, TagType<?>>> CLASS_TO_TYPES
            = Lazy.of(() -> new HashMap<>(PrimitiveTagType.VALUES.length));
    private static final Byte2ObjectArrayMap<TagType<?>> ID_TO_TYPES = new Byte2ObjectArrayMap<>();

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
