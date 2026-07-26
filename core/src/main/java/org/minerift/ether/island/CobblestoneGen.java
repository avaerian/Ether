package org.minerift.ether.island;

import it.unimi.dsi.fastutil.objects.*;
import org.minerift.ether.nms.BlockStateNotFoundException;
import org.minerift.ether.nms.world.block.BlockState;
import org.minerift.ether.util.UnreachableException;

public class CobblestoneGen {

    public static CobblestoneGen.Builder builder() {
        return new Builder();
    }

    public static CobblestoneGen defaults() {
        Builder builder = builder();
        try {
            builder.register(BlockState.of("minecraft:cobblestone"), 75)
                    .register(BlockState.of("minecraft:stone"), 35)
                    .register(BlockState.of("minecraft:copper_ore"), 33)
                    .register(BlockState.of("minecraft:coal_ore"), 45)
                    .register(BlockState.of("minecraft:iron_ore"), 27)
                    .register(BlockState.of("minecraft:gold_ore"), 22)
                    .register(BlockState.of("minecraft:redstone_ore"), 42)
                    .register(BlockState.of("minecraft:lapis_lazuli_ore"), 38)
                    .register(BlockState.of("minecraft:diamond_ore"), 18)
                    .register(BlockState.of("minecraft:emerald_ore"), 17);
        } catch (BlockStateNotFoundException e) {
            throw new UnreachableException("unexpected", e);
        }
        return builder.build();
    }

    public static CobblestoneGen overworld() {
        return defaults();
    }

    public static CobblestoneGen nether() {
        throw new UnreachableException("unimplemented");
    }

    public static CobblestoneGen end() {
        throw new UnreachableException("unimplemented");
    }

    private Object2IntSortedMap<BlockState> weights;

    public CobblestoneGen(Object2IntSortedMap<BlockState> weights) {
        this.weights = Object2IntSortedMaps.unmodifiable(weights);
    }

    // TODO: refactor by moving to more common loot table/weighted reward data structure
    public BlockState selectRandom() {
        int totalWeight = 0;
        for(int i : weights.values()) {
            totalWeight += i;
        }

        throw new UnreachableException("unimplemented");
    }

    public int getWeight(BlockState state) {
        return weights.getInt(state);
    }

    public Object2IntSortedMap<BlockState> getWeightsView() {
        return weights;
    }

    // "mutable" version; mutating should really only occur when many changes
    // are made, then built into an immutable state
    public static class Builder {

        private Object2IntSortedMap<BlockState> weights;

        private Builder(Object2IntSortedMap<BlockState> weights) {
            this.weights = weights;
        }

        private Builder() {
            this.weights = new Object2IntAVLTreeMap<>();
        }

        public Builder register(BlockState state, int weight) {
            weights.put(state, weight);
            return this;
        }

        public int getWeight(BlockState state) {
            return weights.getInt(state);
        }

        public Builder unregister(BlockState state) {
            weights.removeInt(state);
            return this;
        }

        public CobblestoneGen build() {
            return new CobblestoneGen(weights);
        }

    }

}
