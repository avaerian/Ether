package org.minerift.ether.util.nbt.tags.container;

import org.minerift.ether.util.nbt.tags.Tag;

import java.util.Map;

// TODO: for removal after committing for archiving
@Deprecated(forRemoval = true)
public class RootEntryTag extends CompoundTag {

    public RootEntryTag() {
        super();
    }

    public RootEntryTag(String name) {
        super(name);
    }

    public RootEntryTag(String name, Map<String, Tag> tags) {
        super(name, tags);
    }
}
