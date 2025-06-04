package org.minerift.ether.util.nunbt.tags.container;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.minerift.ether.util.nunbt.NbtTraverser;
import org.minerift.ether.util.nunbt.TagCodec;
import org.minerift.ether.util.nunbt.snbt.Snbt;
import org.minerift.ether.util.nunbt.snbt.UnexpectedTokenException;
import org.minerift.ether.util.nunbt.tags.*;

import java.util.*;
import java.util.function.Function;

import static org.minerift.ether.util.nunbt.tags.TagType.*;

public class CompoundTag extends AbstractContainerTag<Map<String, Tag<?>>> {

    protected Map<String, Tag<?>> tags;

    public CompoundTag() {
        this("", new LinkedHashMap<>());
    }

    public CompoundTag(String name) {
        this(name, new LinkedHashMap<>());
    }

    public CompoundTag(String name, Map<String, Tag<?>> tags) {
        this.name = name;
        this.tags = tags;
    }

    @Override
    public TagType getType() {
        return TagType.COMPOUND;
    }

    public void addTag(Tag<?> tag) {
        Preconditions.checkArgument(!tags.containsKey(tag.getName()), "Tag already exists with name " + tag.getName() + " in CompoundTag!");
        tags.put(tag.getName(), tag);
    }

    public Tag<?> getTag(String name) {
        return tags.get(name);
    }

    // NOTE: unsafe; TODO: add safety checks with exception?
    public <T extends Tag<?>> T getTag(String name, Class<T> clazz) {
        return (T) getTag(name);
    }

    @Override
    public void removeTag(Tag<?> tag) {
        removeTag(tag.getName());
    }

    public void removeTag(String name) {
        tags.remove(name);
    }

    private <R, F extends Function<Tag<?>, R>> R getTagValue(String name, TagType expectedType, F none, F some) {
        Tag<?> tag = tags.get(name);
        if(tag == null) {
            return none.apply(null);
        }
        Preconditions.checkArgument(tag.getType() == expectedType,
                tag.getType() + " tag \"" + name + "\" found, but expected type " + expectedType);
        return some.apply(tag);
    }

    private <T> Optional<T> getTagValue(String name, TagType expectedType) {
        return (Optional<T>) getTagValue(name, expectedType, (t) -> Optional.empty(), (t) -> Optional.of(t.getValue()));
    }

    // TODO: add getOrThrow methods to throw NbtTagGetException for better IO exception handling
    //  (for schematic handling, if nbt fails to retrieve, catch nbt except and throw as SchematicFileReadException)

    // TODO: create additional primitive Optional classes to avoid autoboxing if possible?
    public Optional<Byte> getByte(String name) {
        return getTagValue(name, BYTE);
    }

    public Optional<Short> getShort(String name) {
        return getTagValue(name, SHORT);
    }

    public OptionalInt getInt(String name) {
        return getTagValue(name, INT, (t) -> OptionalInt.empty(), (t) -> OptionalInt.of(((IntTag)t).getIntValue()));
    }

    public OptionalLong getLong(String name) {
        return getTagValue(name, LONG, (t) -> OptionalLong.empty(), (t) -> OptionalLong.of(((LongTag)t).getLongValue()));
    }

    public Optional<Float> getFloat(String name) {
        return getTagValue(name, FLOAT);
    }

    public OptionalDouble getDouble(String name) {
        return getTagValue(name, DOUBLE, (t) -> OptionalDouble.empty(), (t) -> OptionalDouble.of(((DoubleTag)t).getDoubleValue()));
    }

    public Optional<String> getString(String name) {
        return getTagValue(name, STRING);
    }

    public <T extends Tag<?>> Optional<ListTag<T>> getList(String name, Class<T> childClazz) {
        return Optional.ofNullable((ListTag<T>) tags.get(name));
    }

    // TODO: add additional checks to ensure types are correct and better exception handling
    public <T extends Tag<?>> Optional<ListTag<T>> getList(String name) {
        return Optional.ofNullable((ListTag<T>) tags.get(name));
    }

    public Optional<CompoundTag> getCompound(String name) {
        return Optional.ofNullable((CompoundTag) tags.get(name));
    }

    public Optional<byte[]> getByteArray(String name) {
        return getTagValue(name, BYTE_ARRAY);
    }

    public Optional<int[]> getIntArray(String name) {
        return getTagValue(name, INT_ARRAY);
    }

    public Optional<long[]> getLongArray(String name) {
        return getTagValue(name, LONG_ARRAY);
    }

    // TODO: refactor by moving this out of CompoundTag and into ListTag ?
    public Optional<double[]> getDoubleArray(String name) {
        Optional<ListTag<DoubleTag>> tag = getList(name);
        return tag.map((listTag) -> {
            List<DoubleTag> tags = listTag.getValue();
            double[] doubles = new double[tags.size()];
            for(int i = 0; i < doubles.length; i++) {
                doubles[i] = tags.get(i).getDoubleValue();
            }
            return doubles;
        });
    }

    @Override
    public Map<String, Tag<?>> getValue() {
        return tags;
    }

    @Override
    public void setValue(Map<String, Tag<?>> value) {
        this.tags = value;
    }

    @Override
    public CompoundTag copy() {
        return new CompoundTag(name, tags);
    }

    @Override
    public CompoundTag copy(@NotNull Function<Map<String, Tag<?>>, Map<String, Tag<?>>> copyContainerFn) {
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
        public void addTag(Tag<?> tag) {
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
                TagType childType = TagType.lookup(childTypeId);
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

                Tag<?> tag = result.type().getCodec().readTag(snbt, name);
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
            for(Tag<?> childTag : tag.getValue().values()) {
                nbt.writeByte(childTag.getType().getId());
                nbt.writeUTF8(childTag.getName());
                childTag.getType().writeTag(nbt, childTag);
            }
            END.writeTag(nbt, EndTag.INSTANCE);
        }

        @Override
        public int skip(NbtTraverser nbt) {
            int bytes = 0;
            do {
                byte childTypeId = nbt.readByte();
                bytes++;
                TagType childType = TagType.lookup(childTypeId);
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
