package org.minerift.ether.world;

import org.minerift.ether.nms.BlockStateNotFoundException;
import org.minerift.ether.nms.world.block.BlockState;
import org.minerift.ether.util.nbt.tags.container.CompoundTag;
import org.minerift.ether.math.Vec3i;

// A block containing a block entity
public class BlockEntityArchetype extends BlockArchetype {

    // TODO: consider updating to use BlockState NBT data?
    private CompoundTag nbtData;

    public BlockEntityArchetype(String data, Vec3i pos, CompoundTag nbtData) throws BlockStateNotFoundException {
        super(BlockState.of(data), pos);
        this.nbtData = nbtData;
        System.out.println("Created new block entity archetype: " + data + " at " + pos + ", nbt data: " + nbtData); // debug
    }

    public BlockEntityArchetype(BlockState<?> state, Vec3i pos, CompoundTag nbt) {
        super(state, pos);
        this.nbtData = nbt;
    }

    public CompoundTag getAsNbt() {
        return nbtData;
    }

    @Override
    public String toString() {
        return String.format("%s -> %s (%s)", pos, state, nbtData);
    }
}
