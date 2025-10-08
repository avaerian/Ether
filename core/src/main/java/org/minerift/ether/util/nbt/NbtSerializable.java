package org.minerift.ether.util.nbt;

import org.minerift.ether.debug.Experimental;
import org.minerift.ether.util.nbt.tags.Tag;

@Experimental
public interface NbtSerializable {
    Tag serializeNbt();
}
