package org.minerift.ether.util.nbt;

import org.minerift.ether.util.nbt.tags.*;

public enum NBTTagType {

    BYTE_ARRAY_TAG(7, "TAG_Byte_Array", ByteArrayTag.class),
    BYTE_TAG(1, "TAG_Byte", ByteTag.class),
    COMPOUND_TAG(10, "TAG_Compound", CompoundTag.class),
    DOUBLE_TAG(6, "TAG_Double", DoubleTag.class),
    END_TAG(0, "TAG_End", EndTag.class),
    FLOAT_TAG(5, "TAG_Float", FloatTag.class),
    INT_ARRAY_TAG(11, "TAG_Int_Array", IntArrayTag.class),
    INT_TAG(3, "TAG_Int", IntTag.class),
    LIST_TAG(9, "TAG_List", ListTag.class),
    LONG_ARRAY_TAG(12, "TAG_Long_Array", LongArrayTag.class),
    LONG_TAG(4, "TAG_Long", LongTag.class),
    SHORT_TAG(2, "TAG_Short", ShortTag.class),
    STRING_TAG(8, "TAG_String", StringTag.class);


    public static NBTTagType getTagType(Class<? extends Tag> clazz) {
        for(NBTTagType type : values()) {
            if(type.getTagClass().equals(clazz)) {
                return type;
            }
        }
        throw new IllegalArgumentException(String.format("Invalid tag class (%s)", clazz.getName()));
    }

    public static NBTTagType getTagType(int id) {
        for(NBTTagType type : values()) {
            if(id == type.getId()) {
                return type;
            }
        }
        throw new IllegalArgumentException(String.format("Invalid tag id (%d)", id));
    }

    private int id;
    private String name;
    private Class<? extends Tag> clazz;
    NBTTagType(int id, String name, Class<? extends Tag> clazz) {
        this.id = id;
        this.name = name;
        this.clazz = clazz;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Class<? extends Tag> getTagClass() {
        return clazz;
    }
}
