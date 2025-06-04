package org.minerift.ether.util.nunbt.tags.container;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.minerift.ether.util.nunbt.NbtTraverser;
import org.minerift.ether.util.nunbt.TagCodec;
import org.minerift.ether.util.nunbt.snbt.Snbt;
import org.minerift.ether.util.nunbt.snbt.UnexpectedTokenException;
import org.minerift.ether.util.nunbt.tags.Tag;
import org.minerift.ether.util.nunbt.tags.TagType;

import java.util.*;
import java.util.function.Function;

import static org.minerift.ether.util.nunbt.tags.TagType.END;

public class ListTag<T extends Tag<?>> extends AbstractContainerTag<List<T>> implements Iterable<T> {

    public static ListTag sizeKnownLater(String name) {
        return new ListTag(name, Collections.emptyList());
    }

    public static ListTag sizeKnownNow(String name, int size) {
        return new ListTag(name, new ArrayList<>(size));
    }

    protected TagType childType;
    protected List<T> tagList;
    @Deprecated private int expectedCapacity;

    public ListTag(String name, List<T> tagList) {
        this(name, tagList, null);
    }

    public ListTag(String name, List<T> tagList, TagType childType) {
        this.name = name;
        this.tagList = tagList;
        this.childType = childType; // TODO: warn about childType being null (should prevent this in other ctor)
    }

    @Deprecated
    public void setChildType(TagType childType) {
        Preconditions.checkArgument(this.childType == null, "childType must not already be set!");
        this.childType = childType;
    }

    public int getCapacity() {
        return Math.max(tagList.size(), expectedCapacity);
    }

    @Override
    public void addTag(Tag<?> tag) {
        Preconditions.checkArgument(tag.getType() == childType, "Requested tag " + tag.getType() + " is not of list child type " + childType);
        tagList.add((T) tag);
    }

    @Override
    public void removeTag(Tag<?> tag) {
        Preconditions.checkArgument(tag.getType() == childType);
        if(!tagList.remove(tag)) {
            throw new NoSuchElementException(tag.getName() + " was not found in " + getName() + ", thus not being removed");
        }
    }

    public void removeTag(int index) {
        tagList.remove(index);
    }

    public T getTag(int i) {
        return tagList.get(i);
    }

    @Override
    public TagType getType() {
        return TagType.LIST;
    }

    public TagType getChildType() {
        return childType;
    }

    @Override
    public List<T> getValue() {
        return tagList;
    }

    @Override
    public void setValue(List<T> value) {
        this.tagList = value;
    }

    @Override
    public ListTag<T> copy() {
        return new ListTag<>(name, tagList, childType);
    }

    @Override
    public ListTag<T> copy(@NotNull Function<List<T>, List<T>> copyContainerFn) {
        return new ListTag<>(name, copyContainerFn.apply(tagList), childType);
    }

    @Deprecated
    public void setValue(List<T> value, int expectedCapacity) {
        this.tagList = value;
        this.expectedCapacity = expectedCapacity;
    }

    // TODO: add method overload for Collectors ($toList(), $toSet(), etc.)
    public <V> List<V> unwrapTags(Function<T, V> tagToValue) {
        return tagList.stream().map(tagToValue).toList();
    }

    @Override
    public String toString() {
        return "ListTag{" +
                "childType=" + childType +
                ", tagList=" + tagList +
                ", expectedCapacity=" + expectedCapacity +
                ", name='" + name + '\'' +
                '}';
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return tagList.iterator();
    }

    public static class LazyListTag extends ListTag<Tag<?>> {
        public LazyListTag(String name) {
            super(name, Collections.emptyList(), END);
        }

        @Override
        public void addTag(Tag<?> tag) {
            if(tagList == Collections.EMPTY_LIST) { // init list tag with first tag
                tagList = new ArrayList<>();
                childType = tag.getType();
            }
            super.addTag(tag);
        }
    }

    public static class Codec implements TagCodec<ListTag<?>> {
        @Override
        public ListTag<?> readTag(NbtTraverser nbt, String name) {
            byte childTypeId = nbt.readByte();
            TagType childType = TagType.lookup(childTypeId);
            int len = nbt.readInt();
            ListTag<?> list = new ListTag<>(name, new ArrayList<>(len), childType);
            for(int i = 0; i < len; i++) {
                list.addTag(childType.readTag(nbt, ""));
            }
            return list;
        }


        // TODO: review handling of empty lists (type = END tag type ?)
        @Override
        public ListTag<?> readTag(Snbt.Parser snbt, String name) throws UnexpectedTokenException {
            snbt.nextIf("[");
            if(snbt.nextIf("]")) {
                return new LazyListTag(name);
            }

            List<Tag<?>> tags = new ArrayList<>();
            TagType childType = END; // undecided
            do {
                Snbt.Token tok = snbt.peek();
                Snbt.TagTypeParserResult result = snbt.getTagType(tok);
                snbt.getTokens().setPos(result.token().getStreamPos());
                if(childType == END) {
                    childType = result.type();
                }

                if(result.type() != childType) {
                    throw new UnexpectedTokenException("Expected " + childType + " token, found " + result.type());
                }

                tags.add(result.type().getCodec().readTag(snbt, ""));

                if(snbt.nextIf("]")) {
                    break;
                }
            } while(snbt.nextIf(","));
            return new ListTag<>(name, tags, childType);
        }

        @Override
        public void writeTag(NbtTraverser nbt, ListTag<?> tag) {
            nbt.writeByte(tag.getChildType().getId());
            nbt.writeInt(tag.getValue().size());
            for(Tag<?> childTag : tag.getValue()) {
                tag.getChildType().writeTag(nbt, childTag);
            }
        }

        @Override
        public int skip(NbtTraverser nbt) {
            byte childTypeId = nbt.readByte();
            TagType childType = TagType.lookup(childTypeId);
            int len = nbt.readInt();

            int bytes = Byte.BYTES + Integer.BYTES;
            for(int i = 0; i < len; i++) {
                bytes += childType.skip(nbt);
            }

            return bytes;
        }
    }
}
