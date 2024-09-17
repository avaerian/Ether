package org.minerift.ether.util.newnbt.tags.container;

import com.google.common.base.Preconditions;
import org.minerift.ether.util.newnbt.NBT;
import org.minerift.ether.util.newnbt.tags.Tag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

public class ListTag extends AbstractContainerTag<List<Tag<?>>> {

    public static ListTag sizeKnownLater(String name) {
        return new ListTag(name, Collections.emptyList());
    }

    public static ListTag sizeKnownNow(String name, int size) {
        return new ListTag(name, new ArrayList<>(size));
    }

    private NBT.TagType childType;
    private List<Tag<?>> tagList;
    private int expectedCapacity;

    public ListTag(String name, List<Tag<?>> tagList) {
        this(name, tagList, null);
    }

    public ListTag(String name, List<Tag<?>> tagList, NBT.TagType childType) {
        this.name = name;
        this.tagList = tagList;
        this.childType = childType;
    }

    public void setChildType(NBT.TagType childType) {
        Preconditions.checkArgument(this.childType == null, "childType must not already be set!");
        this.childType = childType;
    }

    public int getCapacity() {
        return Math.max(tagList.size(), expectedCapacity);
    }

    @Override
    public void addTag(Tag<?> tag) {
        Preconditions.checkArgument(tag.getType() == childType, "Requested tag " + tag.getType() + " is not of list child type " + childType);
        tagList.add(tag);
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

    @Override
    public NBT.TagType getType() {
        return NBT.TagType.LIST_TAG;
    }

    public NBT.TagType getChildType() {
        return childType;
    }

    @Override
    public List<Tag<?>> getValue() {
        return tagList;
    }

    @Override
    public void setValue(List<Tag<?>> value) {
        this.tagList = value;
    }

    public void setValue(List<Tag<?>> value, int expectedCapacity) {
        this.tagList = value;
        this.expectedCapacity = expectedCapacity;
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
}
