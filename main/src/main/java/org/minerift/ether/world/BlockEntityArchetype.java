package org.minerift.ether.world;

import org.minerift.ether.util.nbt.tags.CompoundTag;
import org.minerift.ether.math.Vec3i;

// A block containing a block entity
public class BlockEntityArchetype extends BlockArchetype {

    private CompoundTag nbtData;

    public BlockEntityArchetype(String id, Vec3i pos, CompoundTag nbtData) {
        super(id, pos);
        this.nbtData = nbtData;
    }

    public CompoundTag getNBTData() {
        return nbtData;
    }

    @Override
    public String toString() {
        return String.format("%s -> %s (%s)", pos, id, nbtData);
    }
}
