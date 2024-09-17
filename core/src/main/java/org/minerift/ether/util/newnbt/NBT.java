package org.minerift.ether.util.newnbt;

import org.minerift.ether.util.newnbt.tags.*;
import org.minerift.ether.util.newnbt.tags.array.ByteArrayTag;
import org.minerift.ether.util.newnbt.tags.array.IntArrayTag;
import org.minerift.ether.util.newnbt.tags.array.LongArrayTag;
import org.minerift.ether.util.newnbt.tags.container.AbstractContainerTag;
import org.minerift.ether.util.newnbt.tags.container.CompoundTag;
import org.minerift.ether.util.newnbt.tags.container.ListTag;
import org.minerift.ether.util.newnbt.token.StatelessTokenSchema;

public class NBT {

    public enum TagType {
        END_TAG         ("TAG_End", EndTag.class, null),
        BYTE_TAG        ("TAG_Byte", ByteTag.class, StatelessTokenSchema.BYTE_TAG_VALUE),
        SHORT_TAG       ("TAG_Short", ShortTag.class, StatelessTokenSchema.SHORT_TAG_VALUE),
        INT_TAG         ("TAG_Int", IntTag.class, StatelessTokenSchema.INT_TAG_VALUE),
        LONG_TAG        ("TAG_Long", LongTag.class, StatelessTokenSchema.LONG_TAG_VALUE),
        FLOAT_TAG       ("TAG_Float", FloatTag.class, StatelessTokenSchema.FLOAT_TAG_VALUE),
        DOUBLE_TAG      ("TAG_Double", DoubleTag.class, StatelessTokenSchema.DOUBLE_TAG_VALUE),
        BYTE_ARRAY_TAG  ("TAG_Byte_Array", ByteArrayTag.class, StatelessTokenSchema.BYTE_ARRAY_VALUE),
        STRING_TAG      ("TAG_String", StringTag.class, StatelessTokenSchema.STRING_TAG_VALUE),
        LIST_TAG        ("TAG_List", ListTag.class, StatelessTokenSchema.LIST_TAG_VALUE),
        COMPOUND_TAG    ("TAG_Compound", CompoundTag.class, StatelessTokenSchema.COMPOUND_TAG_VALUE),
        INT_ARRAY_TAG   ("TAG_Int_Array", IntArrayTag.class, StatelessTokenSchema.INT_ARRAY_VALUE),
        LONG_ARRAY_TAG  ("TAG_Long_Array", LongArrayTag.class, StatelessTokenSchema.LONG_ARRAY_VALUE)

        ;

        private static final TagType[] LOOKUP = values();

        public static TagType getType(int id) {
            if(id < 0 || id >= LOOKUP.length) {
                throw new IllegalArgumentException("Invalid tag id " + id);
            }
            return LOOKUP[id];
        }

        public static TagType getType(byte byteId) {
            return getType(byteId & 0xFF);
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
        private final StatelessTokenSchema payloadSchema;

        TagType(String name, Class<? extends Tag> clazz, StatelessTokenSchema tokens) {
            this.name = name;
            this.clazz = clazz;
            this.payloadSchema = tokens;
        }

        public String getName() {
            return name;
        }

        public Class<? extends Tag> getTagClass() {
            return clazz;
        }

        public StatelessTokenSchema getPayloadSchema() {
            return payloadSchema;
        }

        public int getPayloadSchemaSize() {
            return payloadSchema.size();
        }

        public int getId() {
            return ordinal();
        }

        public byte getByteId() {
            return (byte) getId();
        }

        public boolean isContainerType() {
            return AbstractContainerTag.class.isAssignableFrom(clazz);
        }
    }



}
