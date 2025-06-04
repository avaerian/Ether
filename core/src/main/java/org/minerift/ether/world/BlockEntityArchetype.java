package org.minerift.ether.world;

import org.minerift.ether.util.nunbt.tags.container.CompoundTag;
import org.minerift.ether.math.Vec3i;

// A block containing a block entity
public class BlockEntityArchetype extends BlockArchetype implements Archetype {

    private CompoundTag nbtData;

    // FIXME: parameters need to be swapped
    public BlockEntityArchetype(String id, Vec3i pos, CompoundTag nbtData) {
        super(pos, id);
        this.nbtData = nbtData;
    }

    public CompoundTag getNBTData() {
        return nbtData;
    }

    @Override
    public String toString() {
        return String.format("%s -> %s (%s)", pos, data, nbtData);
    }
}
