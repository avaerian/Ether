package org.minerift.ether.util.nbt.tags.container;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.minerift.ether.util.UnreachableException;
import org.minerift.ether.util.nbt.NbtTraverser;
import org.minerift.ether.util.nbt.TagCodec;
import org.minerift.ether.util.nbt.snbt.Snbt;
import org.minerift.ether.util.nbt.snbt.UnexpectedTokenException;
import org.minerift.ether.util.nbt.tags.*;
import org.minerift.ether.util.nbt.tags.array.ByteArrayTag;
import org.minerift.ether.util.nbt.tags.array.IntArrayTag;
import org.minerift.ether.util.nbt.tags.array.LongArrayTag;

import java.util.*;
import java.util.function.UnaryOperator;

import static org.minerift.ether.util.nbt.tags.PrimitiveTagType.*;

public class CompoundTag extends AbstractContainerTag<Map<String, Tag>> {

    protected Map<String, Tag> tags;

    public CompoundTag() {
        this("", new LinkedHashMap<>());
    }

    public CompoundTag(String name) {
        this(name, new LinkedHashMap<>());
    }

    public CompoundTag(String name, Map<String, Tag> tags) {
        this.name = name;
        this.tags = tags;
    }

    @Override
    public TagType<CompoundTag> getType() {
        return TagType.COMPOUND;
    }

    public void addTag(Tag tag) {
        Preconditions.checkArgument(!tags.containsKey(tag.getName()), "Tag already exists with name " + tag.getName() + " in CompoundTag!");
        tags.put(tag.getName(), tag);
    }

    public Tag getTag(String name) {
        return tags.get(name);
    }

    public <T extends Tag> T getTag(String name, Class<? extends T> clazz) {
        return getTag(name, TagType.lookup(clazz));
    }

    public <T extends Tag> T getTag(String name, TagType<T> type) {
        Tag tag = getTag(name);
        if(tag == null) {
            return null;
        }

        if(!tag.is(type)) {
            throw new IllegalArgumentException("Found tag " + name + "; expected type " + type.getTagClass() + ", got " + tag.getType());
        }
        return (T) tag;
    }

    // TODO
    public <T extends Tag> ListTag<T> getListTag(String name, TagType<T> childType) {
        ListTag<?> tag = getTag(name, TagType.LIST);
        //if(tag.isHolding())
        throw new UnreachableException("unimplemented");
    }

    public void removeTag(Tag tag) {
        removeTag(tag.getName());
    }

    public void removeTag(String name) {
        tags.remove(name);
    }

    /*private <R, F extends Function<Tag, R>> R getTagValue(String name, TagType<> expectedType, F none, F some) {
        Tag tag = tags.get(name);
        if(tag == null) {
            return none.apply(null);
        }
        Preconditions.checkArgument(tag.getType() == expectedType,
                tag.getType() + " tag \"" + name + "\" found, but expected type " + expectedType);
        return some.apply(tag);
    }*/

    // TODO: add getOrThrow methods to throw NbtTagGetException for better IO exception handling
    //  (for schematic handling, if nbt fails to retrieve, catch nbt except and throw as SchematicFileReadException)

    // TODO: create additional primitive Optional classes to avoid autoboxing if possible?
    public Optional<Byte> getByte(String name) {
        ByteTag tag = getTag(name, TagType.BYTE);
        return tag != null
                ? Optional.of(tag.getAsByte())
                : Optional.empty();
    }

    public Optional<Short> getShort(String name) {
        ShortTag tag = getTag(name, TagType.SHORT);
        return tag != null
                ? Optional.of(tag.getAsShort())
                : Optional.empty();
    }

    public OptionalInt getInt(String name) {
        IntTag tag = getTag(name, TagType.INT);
        return tag != null
                ? OptionalInt.of(tag.getAsInt())
                : OptionalInt.empty();
    }

    public OptionalLong getLong(String name) {
        LongTag tag = getTag(name, TagType.LONG);
        return tag != null
                ? OptionalLong.of(tag.getAsLong())
                : OptionalLong.empty();
    }

    public Optional<Float> getFloat(String name) {
        FloatTag tag = getTag(name, TagType.FLOAT);
        return tag != null
                ? Optional.of(tag.getAsFloat())
                : Optional.empty();
    }

    public OptionalDouble getDouble(String name) {
        DoubleTag tag = getTag(name, TagType.DOUBLE);
        return tag != null
                ? OptionalDouble.of(tag.getAsDouble())
                : OptionalDouble.empty();
    }

    public Optional<String> getString(String name) {
        StringTag tag = getTag(name, TagType.STRING);
        return tag != null
                ? Optional.of(tag.getValue())
                : Optional.empty();
    }

    public <T extends Tag> Optional<ListTag<T>> getList(String name, @Nullable TagType<T> childType) {
        ListTag<?> tag = getTag(name, TagType.LIST);
        if(tag == null) {
            return Optional.empty();
        }

        if(childType != null && !tag.getChildType().equals(childType)) {
            // Mismatching children types
        }

        return Optional.of((ListTag<T>) tag);
    }

    public <T extends Tag> Optional<ListTag<T>> getList(String name, Class<T> childClazz) {
        return getList(name, TagType.lookup(childClazz));
    }



    public Optional<CompoundTag> getCompound(String name) {
        CompoundTag tag = getTag(name, TagType.COMPOUND);
        return Optional.ofNullable(tag);
    }

    public Optional<byte[]> getByteArray(String name) {
        ByteArrayTag tag = getTag(name, TagType.BYTE_ARRAY);
        return tag != null
                ? Optional.of(tag.getValue())
                : Optional.empty();
    }

    public Optional<int[]> getIntArray(String name) {
        IntArrayTag tag = getTag(name, TagType.INT_ARRAY);
        return tag != null
                ? Optional.of(tag.getValue())
                : Optional.empty();
    }

    public Optional<long[]> getLongArray(String name) {
        LongArrayTag tag = getTag(name, TagType.LONG_ARRAY);
        return tag != null
                ? Optional.of(tag.getValue())
                : Optional.empty();
    }

    // TODO: refactor these methods by remove Optional ????
    // TODO: refactor by moving this out of CompoundTag and into ListTag ?
    public Optional<double[]> getDoubleArray(String name) {
        ListTag<DoubleTag> listTag = getListTag(name, TagType.DOUBLE);
        if(listTag == null) {
            return Optional.empty();
        }

        double[] buf = new double[listTag.getValue().size()];
        for(int i = 0; i < listTag.size(); i++) {
            buf[i] = listTag.getTag(i).getAsDouble();
        }
        return Optional.of(buf);
    }

    public Map<String, Tag> getValue() {
        return tags;
    }

    public void setValue(Map<String, Tag> value) {
        this.tags = value;
    }

    @Override
    public CompoundTag copy() {
        CompoundTag copy = new CompoundTag(name);
        for(Tag tag : getValue().values()) {
            copy.addTag(tag.copy());
        }
        return copy;
    }

    @Override
    public CompoundTag copy(@NotNull UnaryOperator<Map<String, Tag>> copyContainerFn) {
        return new CompoundTag(name, copyContainerFn.apply(tags));
    }

    @Override
    public String toString() {
        return "CompoundTag{" +
                "tags=" + tags +
                ", name='" + name + '\'' +
                '}';
    }

    public static class LazyCompoundTag extends CompoundTag {
        public LazyCompoundTag() {
            super("", Collections.emptyMap());
        }

        public LazyCompoundTag(String name) {
            super(name, Collections.emptyMap());
        }

        @Override
        public void addTag(Tag tag) {
            if(tags == Collections.EMPTY_MAP) {
                tags = new LinkedHashMap<>();
            }
            super.addTag(tag);
        }


    }

    public static class Codec implements TagCodec<CompoundTag> {
        @Override
        public CompoundTag readTag(NbtTraverser nbt, String name) {
            CompoundTag compound = new CompoundTag(name);
            do {
                byte childTypeId = nbt.readByte();
                PrimitiveTagType childType = lookup(childTypeId);
                if(childType == END) {
                    break;
                }

                String childName = nbt.readUTF8();
                NbtTraverser.TagHeader header = new NbtTraverser.TagHeader(childTypeId, childName);
                if(nbt.tagSelector.test(header)) {
                    compound.addTag(childType.readTag(nbt, childName));
                } else {
                    childType.skip(nbt);
                }
            } while(true);

            return compound;
        }

        @Override
        public CompoundTag readTag(Snbt.Parser snbt, String cname) throws UnexpectedTokenException {
            snbt.nextIf("{");
            if(snbt.nextIf("}")) {
                return new LazyCompoundTag(cname);
            }

            CompoundTag compound = new CompoundTag(cname); // TODO: make LazyCompoundTag a singleton that once a tag is added, the mutable compound tag is created
            do {
                String name = snbt.expectName();
                snbt.expect(":");
                Snbt.Token valTok = snbt.peek();
                Snbt.TagTypeParserResult result = snbt.getTagType(valTok);
                System.out.println(result.token());
                snbt.getTokens().setPos(result.token().getStreamPos());

                Tag tag = result.type().codec().readTag(snbt, name);
                compound.addTag(tag);

                if(snbt.nextIf("}")) {
                    break;
                }
            } while (snbt.nextIf(","));
            // TODO: better exception handling for malformed snbt?

            return compound;
        }

        @Override
        public void writeTag(NbtTraverser nbt, CompoundTag tag) {
            for(Tag childTag : tag.getValue().values()) {
                nbt.writeByte(childTag.getType().getId());
                nbt.writeUTF8(childTag.getName());
                ((TagType<Tag>)childTag.getType()).codec().writeTag(nbt, childTag);
            }
            END.writeTag(nbt, EndTag.INSTANCE);
        }

        @Override
        public int skip(NbtTraverser nbt) {
            int bytes = 0;
            do {
                byte childTypeId = nbt.readByte();
                bytes++;
                PrimitiveTagType childType = lookup(childTypeId);
                if(childType == END) {
                    break;
                }
                bytes += STRING.skip(nbt); // child name
                bytes += childType.skip(nbt);
            } while(true);

            return bytes;
        }
    }
}
