package org.minerift.ether.island;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.minerift.ether.nms.world.block.BlockState;

public class IslandValue {
    protected Object2IntOpenHashMap<BlockState> statesToValues;
    public IslandValue(Object2IntOpenHashMap<BlockState> statesToValues) {
        this.statesToValues = statesToValues;
    }

    public BlockState[] getBlockStates() {
        return statesToValues.keySet().toArray(BlockState[]::new);
    }

    public int[] getValues() {
        return statesToValues.values().toArray(new int[0]);
    }

    public Object2IntMap<BlockState> getView() {
        return Object2IntMaps.unmodifiable(statesToValues);
    }

    public static class Mutable extends IslandValue {

        public Mutable(Object2IntOpenHashMap<BlockState> statesToValues) {
            super(statesToValues);
        }

        public Mutable() {
            this(new Object2IntOpenHashMap<>());
        }

        public boolean add(BlockState state, int value) {
            return statesToValues.putIfAbsent(state, value) == 0;
        }

        public boolean set(BlockState state, int value) {
            return statesToValues.put(state, value) == 0;
        }

        public boolean remove(BlockState state) {
            return statesToValues.removeInt(state) != 0;
        }
    }
}
