package org.minerift.ether.util.nbt;

import com.google.common.base.Preconditions;
import org.minerift.ether.util.nbt.tags.*;

public enum NBTTagType {

    END_TAG         ("TAG_End", EndTag.class),
    BYTE_TAG        ("TAG_Byte", ByteTag.class),
    SHORT_TAG       ("TAG_Short", ShortTag.class),
    INT_TAG         ("TAG_Int", IntTag.class),
    LONG_TAG        ("TAG_Long", LongTag.class),
    FLOAT_TAG       ("TAG_Float", FloatTag.class),
    DOUBLE_TAG      ("TAG_Double", DoubleTag.class),
    BYTE_ARRAY_TAG  ("TAG_Byte_Array", ByteArrayTag.class),
    STRING_TAG      ("TAG_String", StringTag.class),
    LIST_TAG        ("TAG_List", ListTag.class),
    COMPOUND_TAG    ("TAG_Compound", CompoundTag.class),
    INT_ARRAY_TAG   ("TAG_Int_Array", IntArrayTag.class),
    LONG_ARRAY_TAG  ("TAG_Long_Array", LongArrayTag.class);

    public static NBTTagType getTagType(Class<? extends Tag> clazz) {
        for(NBTTagType type : values()) {
            if(type.getTagClass().equals(clazz)) {
                return type;
            }
        }
        throw new IllegalArgumentException(String.format("Invalid tag class (%s)", clazz.getName()));
    }

    public static NBTTagType getTagType(int id) {
        final NBTTagType[] types = NBTTagType.values();
        Preconditions.checkArgument(id < types.length, String.format("Invalid tag id (%d)", id));
        return types[id];
    }

    private final String name;
    private final Class<? extends Tag> clazz;
    NBTTagType(String name, Class<? extends Tag> clazz) {
        this.name = name;
        this.clazz = clazz;
    }

    public int getId() {
        return ordinal();
    }

    public String getName() {
        return name;
    }

    public Class<? extends Tag> getTagClass() {
        return clazz;
    }
}
