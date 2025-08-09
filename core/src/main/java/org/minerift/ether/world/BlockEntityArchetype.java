package org.minerift.ether.world;

import org.minerift.ether.nms.BlockStateNotFoundException;
import org.minerift.ether.nms.world.BlockState;
import org.minerift.ether.util.nbt.tags.container.CompoundTag;
import org.minerift.ether.math.Vec3i;

// A block containing a block entity
public class BlockEntityArchetype extends BlockArchetype implements Archetype {

    private CompoundTag nbtData;

    public BlockEntityArchetype(String data, Vec3i pos, CompoundTag nbtData) throws BlockStateNotFoundException {
        super(BlockState.of(data), pos);
        this.nbtData = nbtData;
        System.out.println("Created new block entity archetype: " + data + " at " + pos + ", nbt data: " + nbtData);
    }

    // FIXME: parameters need to be swapped
    public BlockEntityArchetype(BlockState<?> state, Vec3i pos, CompoundTag nbtData) {
        super(state, pos);
        this.nbtData = nbtData;
    }

    public CompoundTag getNbtData() {
        return nbtData;
    }

    @Override
    public String toString() {
        return String.format("%s -> %s (%s)", pos, state, nbtData);
    }
}
