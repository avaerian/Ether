package org.minerift.ether.util.nbt.tags.container;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.minerift.ether.util.Either;
import org.minerift.ether.util.nbt.*;
import org.minerift.ether.util.nbt.snbt.Snbt;
import org.minerift.ether.util.nbt.snbt.UnexpectedTokenException;
import org.minerift.ether.util.nbt.tags.*;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import static org.minerift.ether.util.nbt.tags.TagTypes.*;

public class CompoundTag extends AbstractContainerTag<Map<String, Tag>> implements Iterable<Map.Entry<String, Tag>> {

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
        return TagTypes.COMPOUND;
    }

    public void addTag(Tag tag) {
        addTag(tag, false);
    }

    public void addTag(Tag tag, boolean replace) {
        if(!replace) {
            Preconditions.checkArgument(!tags.containsKey(tag.getName()), "Tag already exists with name " + tag.getName() + " in CompoundTag");
        }
        tags.put(tag.getName(), tag);
    }

    public boolean has(String name) {
        return tags.containsKey(name);
    }

    public boolean has(String name, TagType<?> type) {
        Tag tag = tags.get(name);
        return tag != null && tag.is(type);
    }

    public <T extends Tag> T getTag(String name, @NotNull Class<T> clazz) throws NoTagFoundException, MismatchedTypeException {
        return getTag(name, lookup(clazz));
    }

    public <T extends Tag> T getTag(String name, @NotNull TagType<T> type) throws NoTagFoundException, MismatchedTypeException {
        Tag tag = tags.get(name);
        if(tag == null) {
            throw new NoTagFoundException("Tag " + name + " not found in compound tag");
        }

        if(!tag.is(type)) {
            throw new MismatchedTypeException("Found tag " + name + "; expected type " + type + ", got " + tag.getType());
        }
        return (T) tag;
    }

    public <T extends Tag, E extends Exception> T getTag(String name, @NotNull TagType<T> type, Function<NbtException, E> ex) throws E {
        try {
            return getTag(name, type);
        } catch (NoTagFoundException | MismatchedTypeException e) {
            throw ex.apply(e);
        }
    }

    // Return tag, or null if not found
    public Tag tryGetTag(String name) {
        return tags.get(name);
    }

    public <T extends Tag> T tryGetTag(String name, Class<T> clazz) {
        try {
            return getTag(name, lookup(clazz));
        } catch (NoTagFoundException | MismatchedTypeException e) {
            return null;
        }
        //return tryGetTag(name, TagTypes.lookup(clazz)); // removed due to confusion
    }

    public <T extends Tag> T tryGetTag(String name, TagType<T> type) {
        try {
            return getTag(name, type);
        } catch (NoTagFoundException | MismatchedTypeException e) {
            return null;
        }
    }

    public Either<Tag, NbtException> getTagResult(String name) {
        Tag tag = tags.get(name);
        return tag != null
                ? Either.left(tag)
                : Either.right(new NoTagFoundException("Tag " + name + " not found in compound tag"));
    }

    // TODO: consider updating this to TagResult (extends Either) with more util/retrieval methods (unwrap <- runtime exception, getOrThrow, etc.)
    public <T extends Tag> Either<T, NbtException> getTagResult(String name, TagType<T> type) {
        Tag tag = tags.get(name);
        if(tag == null) {
            return Either.right(new NoTagFoundException("Tag " + name + " not found in compound tag"));
        }

        if(!tag.is(type)) {
            return Either.right(new MismatchedTypeException("Found tag " + name + "; expected type " + type + ", got " + tag.getType()));
        }
        return Either.left((T) tag);
    }

    public void removeTag(Tag tag) {
        removeTag(tag.getName());
    }

    public void removeTag(String name) {
        tags.remove(name);
    }

    @Override
    public void accept(TagVisitor visit) {
        visit.visitCompound(this);
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

    public byte getByte(String name) throws NoTagFoundException, MismatchedTypeException {
        return getTag(name, BYTE).getAsByte();
    }

    public <E extends Exception> byte getByte(String name, Function<NbtException, E> ex) throws E {
        try {
            return getTag(name, BYTE).getAsByte();
        } catch (NoTagFoundException | MismatchedTypeException e) {
            throw ex.apply(e);
        }
    }


    public short getShort(String name) throws NoTagFoundException, MismatchedTypeException {
        return getTag(name, SHORT).getAsShort();
    }

    public <E extends Exception> short getShort(String name, Function<NbtException, E> ex) throws E {
        try {
            return getTag(name, SHORT).getAsShort();
        } catch (NoTagFoundException | MismatchedTypeException e) {
            throw ex.apply(e);
        }
    }


    public int getInt(String name) throws NoTagFoundException, MismatchedTypeException {
        return getTag(name, INT).getAsInt();
    }

    public <E extends Exception> int getInt(String name, Function<NbtException, E> ex) throws E {
        try {
            return getTag(name, INT).getAsInt();
        } catch (NoTagFoundException | MismatchedTypeException e) {
            throw ex.apply(e);
        }
    }


    public long getLong(String name) throws NoTagFoundException, MismatchedTypeException {
        return getTag(name, LONG).getAsLong();
    }

    public <E extends Exception> long getLong(String name, Function<NbtException, E> ex) throws E {
        try {
            return getTag(name, LONG).getAsLong();
        } catch (NoTagFoundException | MismatchedTypeException e) {
            throw ex.apply(e);
        }
    }


    public float getFloat(String name) throws NoTagFoundException, MismatchedTypeException {
        return getTag(name, FLOAT).getAsFloat();
    }

    public <E extends Exception> float getFloat(String name, Function<NbtException, E> ex) throws E {
        try {
            return getTag(name, FLOAT).getAsFloat();
        } catch (NoTagFoundException | MismatchedTypeException e) {
            throw ex.apply(e);
        }
    }


    public double getDouble(String name) throws NoTagFoundException, MismatchedTypeException {
        return getTag(name, DOUBLE).getAsDouble();
    }

    public <E extends Exception> double getDouble(String name, Function<NbtException, E> ex) throws E {
        try {
            return getTag(name, DOUBLE).getAsDouble();
        } catch (NoTagFoundException | MismatchedTypeException e) {
            throw ex.apply(e);
        }
    }

    public String getString(String name) throws NoTagFoundException, MismatchedTypeException {
        return getTag(name, STRING).getStrVal();
    }

    public <E extends Exception> String getString(String name, Function<NbtException, E> ex) throws E {
        try {
            return getTag(name, STRING).getStrVal();
        } catch (NoTagFoundException | MismatchedTypeException e) {
            throw ex.apply(e);
        }
    }


    public <T extends Tag> ListTag<T> tryGetList(String name, @NotNull TagType<T> childType) {
        try {
            return getList(name, childType);
        } catch (NoTagFoundException | MismatchedTypeException | MismatchedChildTypeException e) {
            return null;
        }
    }

    // Child type should not be null; use the getTag method instead for getting untyped list tags
    public <T extends Tag> ListTag<T> getList(String name, @NotNull TagType<T> childType)
            throws NoTagFoundException, MismatchedTypeException, MismatchedChildTypeException {
        Preconditions.checkNotNull(childType);
        ListTag<?> tag = getTag(name, TagTypes.LIST);
        ListTag<T> cast = tag.withChildType(childType);

        if(cast == null) {
            throw new MismatchedChildTypeException("Expected child type"); // FIXME: write rest of message
        }
        return cast;
    }

    public <T extends Tag> ListTag<T> getList(String name, Class<T> childClazz)
            throws NoTagFoundException, MismatchedTypeException, MismatchedChildTypeException {
        return getList(name, lookup(childClazz));
    }

    public <T extends Tag, E extends Exception> ListTag<T> getList(String name, TagType<T> childType, Function<NbtException, E> ex) throws E {
        try {
            return getList(name, childType);
        } catch (NoTagFoundException | MismatchedTypeException | MismatchedChildTypeException e) {
            throw ex.apply(e);
        }
    }

    public <T extends Tag, E extends Exception> ListTag<T> getList(String name, Class<T> clazz, Function<NbtException, E> ex) throws E {
        try {
            return getList(name, clazz);
        } catch (NoTagFoundException | MismatchedTypeException | MismatchedChildTypeException e) {
            throw ex.apply(e);
        }
    }


    public <T extends Tag> Either<ListTag<T>, NbtException> getListTagResult(String name, TagType<T> childType) {
        try {
            ListTag<T> tag = getList(name, childType);
            return Either.left(tag);
        } catch (NoTagFoundException | MismatchedTypeException | MismatchedChildTypeException e) {
            return Either.right(e);
        }
    }


    public CompoundTag getCompound(String name) throws NoTagFoundException, MismatchedTypeException {
        return getTag(name, COMPOUND);
    }

    public <E extends Exception> CompoundTag getCompound(String name, Function<NbtException, E> ex) throws E {
        try {
            return getTag(name, COMPOUND);
        } catch (NoTagFoundException | MismatchedTypeException e) {
            throw ex.apply(e);
        }
    }


    public byte[] getByteArray(String name) throws NoTagFoundException, MismatchedTypeException {
        return getTag(name, BYTE_ARRAY).getValue();
    }

    public <E extends Exception> byte[] getByteArray(String name, Function<NbtException, E> ex) throws E {
        try {
            return getTag(name, BYTE_ARRAY).getValue();
        } catch (NoTagFoundException | MismatchedTypeException e) {
            throw ex.apply(e);
        }
    }


    public int[] getIntArray(String name) throws NoTagFoundException, MismatchedTypeException {
        return getTag(name, INT_ARRAY).getValue();
    }

    public <E extends Exception> int[] getIntArray(String name, Function<NbtException, E> ex) throws E {
        try {
            return getTag(name, INT_ARRAY).getValue();
        } catch (NoTagFoundException | MismatchedTypeException e) {
            throw ex.apply(e);
        }
    }

    public long[] getLongArray(String name) throws NoTagFoundException, MismatchedTypeException {
        return getTag(name, LONG_ARRAY).getValue();
    }

    public <E extends Exception> long[] getLongArray(String name, Function<NbtException, E> ex) throws E {
        try {
            return getTag(name, LONG_ARRAY).getValue();
        } catch (NoTagFoundException | MismatchedTypeException e) {
            throw ex.apply(e);
        }
    }


    // extended NBT
    // NOTE: this uses a ListTag to read the double array; may be confusing
    //       from the abstraction; should change this
    public double[] getDoubleArray(String name) throws NoTagFoundException, MismatchedTypeException, MismatchedChildTypeException {
        ListTag<DoubleTag> listTag = getList(name, TagTypes.DOUBLE);
        double[] buf = new double[listTag.getValue().size()];
        for(int i = 0; i < listTag.size(); i++) {
            buf[i] = listTag.getTag(i).getAsDouble();
        }
        return buf;
    }

    public <E extends Exception> double[] getDoubleArray(String name, Function<NbtException, E> ex) throws E {
        try {
            return getDoubleArray(name);
        } catch (NoTagFoundException | MismatchedTypeException | MismatchedChildTypeException e) {
            throw ex.apply(e);
        }
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
        System.out.println("Values: " + tags.values());
        for(Tag tag : tags.values()) {
            System.out.println(tag);
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

    @Override
    public @NotNull Iterator<Map.Entry<String, Tag>> iterator() {
        return tags.entrySet().iterator();
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
        public CompoundTag readTag(NbtTraverser nbt, String name) throws NbtReadException {
            CompoundTag compound = new CompoundTag(name);
            do {
                byte childTypeId = nbt.readByte();
                TagType<?> childType = TagTypes.lookup(childTypeId);
                if(childType == END) {
                    break;
                }

                String childName = nbt.readUTF8();
                NbtTraverser.TagHeader header = new NbtTraverser.TagHeader(childTypeId, childName);
                if(nbt.tagSelector.test(header)) {
                    compound.addTag(childType.codec().readTag(nbt, childName));
                } else {
                    childType.codec().skip(nbt);
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
            // TODO: better exception handling for malformed snbt

            return compound;
        }

        @Override
        public void writeTag(NbtTraverser nbt, CompoundTag tag) {
            for(Tag childTag : tag.getValue().values()) {
                nbt.writeByte(childTag.getType().getId());
                nbt.writeUTF8(childTag.getName());
                ((TagType<Tag>)childTag.getType()).codec().writeTag(nbt, childTag);
            }
            END.codec().writeTag(nbt, EndTag.INST);
        }

        @Override
        public int skip(NbtTraverser nbt) {
            int bytes = 0;
            do {
                byte childTypeId = nbt.readByte();
                bytes++;
                TagType<?> childType = TagTypes.lookup(childTypeId);
                if(childType == END) {
                    break;
                }
                bytes += STRING.codec().skip(nbt); // child name
                bytes += childType.codec().skip(nbt);
            } while(true);

            return bytes;
        }
    }
}
