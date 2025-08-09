package org.minerift.ether.util.nbt.tags.container;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.minerift.ether.debug.Debug;
import org.minerift.ether.util.Note;
import org.minerift.ether.util.nbt.NbtTraverser;
import org.minerift.ether.util.nbt.TagCodec;
import org.minerift.ether.util.nbt.snbt.Snbt;
import org.minerift.ether.util.nbt.snbt.UnexpectedTokenException;
import org.minerift.ether.util.nbt.tags.*;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import static java.lang.String.format;
import static org.minerift.ether.util.nbt.tags.TagType.END;

public class ListTag<T extends Tag> extends AbstractContainerTag<List<T>> implements Iterable<T> {// TODO: review Iterable<T> vs Iterable<List<Tag>> /*permits ListTag.Untyped*/

    public static ListTag<?> empty(String name) {
        return new ListTag<>(name, TagType.END, Collections.emptyList());
    }

    protected TagType<T> childType;
    protected List<T> tagList;

    @Deprecated
    public ListTag(String name, List<T> tagList) {
        this(name, null, tagList);
    }

    public ListTag(String name, TagType<T> childType) {
        this(name, childType, Collections.emptyList());
    }

    public ListTag(String name, TagType<T> childType, List<T> tagList) {
        this.name = name;
        this.childType = childType;
        this.tagList = tagList;
    }

    public ListTag(String name, TagType<T> childType, Supplier<List<T>> tagList) {
        loadListTag(name, childType, tagList);
    }

    // TODO: review; ???????????
    @Note("This method acts as the constructor for this class")
    protected void loadListTag(String name, TagType<T> childType, Supplier<List<T>> tagList) {
        this.name = name;
        this.tagList = tagList.get();
        this.childType = childType; // TODO: warn about childType being null (should prevent this in other ctor)
    }

    @Debug
    public static void main(String[] args) {
        ListTag<StringTag> test = new ListTag<>("test_list", TagType.STRING, new LinkedList<>());
        System.out.println(test.tagList.getClass());
    }

    // TODO: remove dead methods, classes, code, etc.; clean up codebase

    // Transform an empty ListTag (one which may not be typed) into a typed one
    public <U extends Tag> ListTag<U> transform(TagType<U> type) {
        if(!tagList.isEmpty()) {
            throw new UnsupportedOperationException("Unable to transform tag list with " + tagList.size() + " elements (needs to be empty)");
        }
        // TODO: review this code; may throw exception
        ListTag<U> transform = (ListTag<U>) this;
        transform.childType = type;
        return transform;
    }

    public void addTag(T tag) {
        Preconditions.checkArgument(tag.getType() == childType, "Requested tag " + tag.getType() + " is not of list child type " + childType);
        if(tagList == Collections.EMPTY_LIST) {
            this.tagList = new ArrayList<>();
        }
        tagList.add(tag); // TODO: if appending first tag, set child type
    }

    // TODO
    public boolean tryAddTag(Tag tag) {
        try {
            addTagOrThrow(tag);
            return true;
        } catch (IllegalArgumentException ex) {
            // TODO: logger
            return false;
        }


    }

    public void addTagOrThrow(Tag tag) throws IllegalArgumentException {
        addTagOrThrow(tag, () -> new IllegalArgumentException(
                format("List tag '%s' contains %s tags; tag '%s' is type %s",
                        name, childType.getPrimitiveType().name(), tag.getName(), tag.getPrimitiveType().name())
        ));
    }

    public <E extends Exception> void addTagOrThrow(Tag tag, Supplier<E> exception) throws E {
        if( !tag.is(childType) ) {
            throw exception.get();
        }
        tagList.add((T) tag);
    }

    public void removeTag(T tag) {
        Preconditions.checkArgument(tag.getType() == childType);
        if(!tagList.remove(tag)) {
            // TODO: review this; exception not necessary???
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
    public TagType<ListTag> getType() {
        return TagType.LIST;
    }

    public TagType<T> getChildType() {
        return childType;
    }

    public boolean isEmptyType() {
        return childType == END && tagList == Collections.EMPTY_LIST;
    }

    // Test if this List Tag is holding elements of requested type
    public boolean childTypeIs(TagType<?> type) {
        return childType.equals(type);
    }

    public int size() {
        return tagList.size();
    }

    public List<T> getValue() {
        return tagList;
    }

    public void setValue(List<T> value) {
        this.tagList = value;
    }

    @Deprecated // TODO
    @Override
    public ListTag<T> copy() {
        List<T> newList = size() != 0 ? new ArrayList<>(size()) : Collections.emptyList();
        // TODO:
        //  ListTag<T> copy = new ListTag<>(name, childType, );

        if(tagList.isEmpty()) {
            return new ListTag<>(name, childType);
        }


        return null;
        //return copy;
    }

    @Override
    public ListTag<T> copy(@NotNull UnaryOperator<List<T>> copyContainerFn) {
        return new ListTag<>(name, childType, copyContainerFn.apply(tagList));
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
                ", name='" + name + '\'' +
                '}';
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return tagList.iterator();
    }

    public static class Codec implements TagCodec<ListTag/*<?>*/> {
        @Override
        public ListTag<?> readTag(NbtTraverser nbt, String name) {
            byte childTypeId = nbt.readByte();
            //PrimitiveTagType childType = PrimitiveTagType.lookup(childTypeId);
            TagType childType = TagType.lookup(childTypeId);
            TagCodec childCodec = childType.codec();
            int len = nbt.readInt();
            ListTag<Tag> list = new ListTag<>(name, childType, new ArrayList<>(len));
            for(int i = 0; i < len; i++) {
                Tag tag = childCodec.readTag(nbt, "");
                list.addTag(tag);
            }
            return list;
        }


        // TODO: review handling of empty lists (type = END tag type ?)
        @Override
        public ListTag<?> readTag(Snbt.Parser snbt, String name) throws UnexpectedTokenException {
            snbt.nextIf("[");
            if(snbt.nextIf("]")) {
                return ListTag.empty(name);
            }

            List<Tag> tags = new ArrayList<>();
            //PrimitiveTagType childType = END; // undecided
            TagType childType = END;
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

                tags.add(result.type().codec().readTag(snbt, ""));

                if(snbt.nextIf("]")) {
                    break;
                }
            } while(snbt.nextIf(","));
            return new ListTag<>(name, childType, tags);
        }

        @Override
        public void writeTag(NbtTraverser nbt, ListTag list) {
            nbt.writeByte(list.getChildType().getId());
            nbt.writeInt(list.getValue().size());
            TagCodec childCodec = list.getChildType().codec();
            for(Tag tag : (ListTag<Tag>)list) {
                childCodec.writeTag(nbt, tag);
            }
        }

        @Override
        public int skip(NbtTraverser nbt) {
            byte childTypeId = nbt.readByte();
            PrimitiveTagType childType = PrimitiveTagType.lookup(childTypeId);
            int len = nbt.readInt();

            int bytes = Byte.BYTES + Integer.BYTES;
            for(int i = 0; i < len; i++) {
                bytes += childType.skip(nbt);
            }

            return bytes;
        }
    }
}
