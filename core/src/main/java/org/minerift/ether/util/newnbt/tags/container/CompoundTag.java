package org.minerift.ether.util.newnbt.tags.container;

import com.google.common.base.Preconditions;
import org.minerift.ether.util.newnbt.NBT;
import org.minerift.ether.util.newnbt.tags.Tag;

import java.util.HashMap;
import java.util.Map;

public class CompoundTag extends AbstractContainerTag<Map<String, Tag<?>>> {

    private Map<String, Tag<?>> tags;

    public CompoundTag() {
        this("", new HashMap<>());
    }

    public CompoundTag(String name) {
        this(name, new HashMap<>());
    }

    public CompoundTag(String name, Map<String, Tag<?>> tags) {
        this.name = name;
        this.tags = tags;
    }

    @Override
    public NBT.TagType getType() {
        return NBT.TagType.COMPOUND_TAG;
    }

    public void addTag(Tag<?> tag) {
        Preconditions.checkArgument(!tags.containsKey(tag.getName()), "Tag already exists with name " + tag.getName() + " in CompoundTag!");
        tags.put(tag.getName(), tag);
    }

    @Override
    public void removeTag(Tag<?> tag) {
        removeTag(tag.getName());
    }

    public void removeTag(String name) {
        tags.remove(name);
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
    public String toString() {
        return "CompoundTag{" +
                "tags=" + tags +
                ", name='" + name + '\'' +
                '}';
    }
}
