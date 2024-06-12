package org.minerift.ether.util.newnbt;

import org.minerift.ether.util.nbt.tags.*;

public class NBT {

    public enum TagType {
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
        LONG_ARRAY_TAG  ("TAG_Long_Array", LongArrayTag.class)

        ;

        private static final TagType[] LOOKUP = values();

        public static TagType getType(int id) {
            if(id < 0 || id >= LOOKUP.length) {
                throw new IllegalArgumentException("Invalid tag id " + id);
            }
            return LOOKUP[id];
        }

        public static TagType getType(Class<? extends Tag> tagClazz) {
            for(TagType type : LOOKUP) {
                if(type.clazz.equals(tagClazz)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Invalid tag class " + tagClazz.getName());
        }

        private final String name;
        private final Class<? extends Tag> clazz;
        TagType(String name, Class<? extends Tag> clazz) {
            this.name = name;
            this.clazz = clazz;
        }

        public String getName() {
            return name;
        }

        public Class<? extends Tag> getTagClass() {
            return clazz;
        }

        public int getId() {
            return ordinal();
        }
    }



}
