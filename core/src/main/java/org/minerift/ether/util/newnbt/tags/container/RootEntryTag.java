package org.minerift.ether.util.newnbt.tags.container;

import org.minerift.ether.util.newnbt.tags.Tag;

import java.util.Map;

public class RootEntryTag extends CompoundTag {

    public RootEntryTag() {
        super();
    }

    public RootEntryTag(String name) {
        super(name);
    }

    public RootEntryTag(String name, Map<String, Tag<?>> tags) {
        super(name, tags);
    }
}
