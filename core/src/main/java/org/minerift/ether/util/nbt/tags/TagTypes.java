package org.minerift.ether.util.nbt.tags;

import com.google.common.base.Supplier;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectOpenHashMap;
import org.minerift.ether.util.nbt.NbtException;
import org.minerift.ether.util.nbt.TagCodec;
import org.minerift.ether.util.nbt.tags.array.ByteArrayTag;
import org.minerift.ether.util.nbt.tags.array.IntArrayTag;
import org.minerift.ether.util.nbt.tags.array.LongArrayTag;
import org.minerift.ether.util.nbt.tags.container.CompoundTag;
import org.minerift.ether.util.nbt.tags.container.ListTag;
import org.minerift.ether.util.nbt.tags.container.NoTagTypeFoundException;

import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;

public class TagTypes {

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

    private static final Map<String, TagType<?>> NAME_TO_TYPE = new HashMap<>();
    private static final Map<Class<? extends Tag>, TagType<?>> CLASS_TO_TYPE = new HashMap<>();
    private static final Byte2ObjectMap<TagType<?>> ID_TO_TYPE = new Byte2ObjectOpenHashMap<>();

    static {
        END = register(0, "End", EndTag.class, EndTag.Codec::new);

        BYTE = register(1, "Byte", ByteTag.class, ByteTag.Codec::new);
        SHORT = register(2, "Short", ShortTag.class, ShortTag.Codec::new);
        INT = register(3, "Int", IntTag.class, IntTag.Codec::new);
        LONG = register(4, "Long", LongTag.class, LongTag.Codec::new);
        FLOAT = register(5, "Float", FloatTag.class, FloatTag.Codec::new);
        DOUBLE = register(6, "Double", DoubleTag.class, DoubleTag.Codec::new);

        STRING = register(8, "String", StringTag.class, StringTag.Codec::new);

        BYTE_ARRAY  = register(7, "Byte_Array", ByteArrayTag.class, ByteArrayTag.Codec::new);
        INT_ARRAY   = register(11, "Int_Array", IntArrayTag.class, IntArrayTag.Codec::new);
        LONG_ARRAY  = register(12, "Long_Array", LongArrayTag.class, LongArrayTag.Codec::new);

        LIST = register(9, "List", ListTag.class, ListTag.Codec::new);
        COMPOUND = register(10, "Compound", CompoundTag.class, CompoundTag.Codec::new);
    } // plan is extended nbt types will have most significant bit set (will be negative)

    public static <T extends Tag> TagType<T> register(int id, String name, Class<T> tagClazz, Supplier<TagCodec<T>> codec) {
        if(id > Byte.MAX_VALUE) {
            throw new IllegalArgumentException("Id " + id + " needs to be a byte id: keep in range(-128-127)");
        }
        return register(new TagType<>((byte)id, name, tagClazz, codec));
    }

    public static <T extends Tag> TagType<T> register(byte id, String name, Class<T> tagClazz, Supplier<TagCodec<T>> codec) {
        return register(new TagType<>(id, name, tagClazz, codec));
    }

    //@NeedsTesting
    public static <T extends Tag> TagType<T> register(TagType<T> type) {
        if((CLASS_TO_TYPE.putIfAbsent(type.getTagClass(), type) != null
            ^ ID_TO_TYPE.putIfAbsent(type.getId(), type) != null)
            ^ NAME_TO_TYPE.putIfAbsent(type.getRawName(), type) != null) {

            throw new IllegalStateException("Tag type registry seems mismatched; malformed");
        }
        TagType<?> res = ID_TO_TYPE.get(type.getId());
        if(res != type) {
            throw new RuntimeException(new NbtException("Tag type already exists with id " + type.getId()));
        }
        return type;
    }

    public static TagType<?> lookup(byte id) {
        return ID_TO_TYPE.get(id);
    }

    public static <T extends Tag> TagType<T> lookup(Class<T> clazz) {
        return (TagType<T>) CLASS_TO_TYPE.get(clazz);
    }

    public static TagType<?> lookupOrThrow(byte id) throws NoTagTypeFoundException {
        TagType<?> res = lookup(id);
        if(res == null) {
            throw new NoTagTypeFoundException(format("Tag type with id %d (0x%02X) not found", id, id));
        }
        return res;
    }

    public static <E extends Exception> TagType<?> lookupOrThrow(byte id, java.util.function.Supplier<E> ex) throws E {
        TagType<?> res = lookup(id);
        if(res == null) {
            throw ex.get();
        }
        return res;
    }

    public static <T extends Tag> TagType<T> lookupOrThrow(Class<T> clazz) throws NoTagTypeFoundException {
        TagType<T> res = lookup(clazz);
        if(res == null) {
            throw new NoTagTypeFoundException(format("Tag type (%s) not found", clazz));
        }
        return res;
    }

    public static <T extends Tag, E extends Exception> TagType<T> lookupOrThrow(Class<T> clazz, java.util.function.Supplier<E> ex) throws E {
        TagType<T> res = lookup(clazz);
        if(res == null) {
            throw ex.get();
        }
        return res;
    }
}
