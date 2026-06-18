package org.minerift.ether.nms.v1_20_R2.data;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import org.minerift.ether.math.Maths;
import org.minerift.ether.nms.world.block.Attribute;
import org.minerift.ether.nms.world.block.Attributes;
import org.minerift.ether.schematic.transform.Axis;
import org.minerift.ether.schematic.transform.Direction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.String.format;
import static net.minecraft.core.Direction.*;
import static net.minecraft.core.Direction.Axis.*;

public class AttributeRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(AttributeRegistry.class);

    private static AttributeRegistry INST;

    public static AttributeRegistry access() {
        if(INST == null) {
            INST = new AttributeRegistry();
        }
        return INST;
    }

    private Entry<?>[] mappings;
    private AttributeLookup attrLookup;

    private AttributeRegistry() {
        this.mappings = new Entry[Attributes.ATTRIBUTES.length];
        this.attrLookup = new AttributeLookup(mappings.length);

        register(Attributes.HORIZONTAL_FACING, BlockStateProperties.HORIZONTAL_FACING, DirectionAttrCodec.INST);
        register(Attributes.FACING, BlockStateProperties.FACING, DirectionAttrCodec.INST);
        register(Attributes.AXIS, BlockStateProperties.AXIS, AxisAttrCodec.INST);
        register(Attributes.LIT, BlockStateProperties.LIT, BoolAttributeCodec.INST);
        register(Attributes.FALLING, BlockStateProperties.FALLING, BoolAttributeCodec.INST);
    }

    private <A extends Attribute<T>, T, NT extends Comparable<NT>> void register(A attr,
                                                                             Property<NT> mapping,
                                                                             AttributeCodec<T, NT> codec) {
        mappings[attr.getId()] = new Entry<>(attr, mapping, codec);
        attrLookup.registerPropId(attr.getId(), mapping.getId());
    }

    public Entry<?> lookup(Property<?> prop) {
        int idx = attrLookup.findIndex(prop.getId());
        if(idx < 0) {
            throw new IllegalArgumentException(
                    format("Property %s (%d) has no mapping Attribute", prop.getName(), prop.getId()));
        }
        return mappings[idx];
    }

    public <T> Entry<T> lookup(Attribute<T> attr) {
        if(!Maths.inRange(0, mappings.length, attr.getId())) {
            throw new IllegalArgumentException(format("Attribute '%s' has no registered mapping", attr.getName()));
        }
        return (Entry<T>) mappings[attr.getId()];
    }

    public Property<?> lookupProp(Attribute<?> attr) {
        return lookup(attr).mapping;
    }

    public Attribute<?> lookupAttr(Property<?> prop) {
        return lookup(prop).attr;
    }

    public <T> boolean hasAttr(BlockState nState, Attribute<T> attr) {
        return nState.hasProperty(lookupProp(attr));
    }

    // Returns the value, or null if state doesn't have the property
    public <T, NT extends Comparable<NT>> T tryGetValue(BlockState state, Attribute<T> attr) {
        Entry<T> entry = lookup(attr);
        if(!state.hasProperty(entry.mapping)) {
            return null;
        }
        NT nVal = state.getValue((Property<NT>) entry.mapping);
        return ((AttributeCodec<T, NT>)entry.codec).fromNative(nVal);
    }

    public <T, NT extends Comparable<NT>> T getValue(BlockState state, Attribute<T> attr) {
        Entry<T> entry = (Entry<T>) mappings[attr.getId()];
        NT val = state.getValue((Property<NT>) entry.mapping);
        return ((AttributeCodec<T, NT>)entry.codec).fromNative(val); // TODO: test
    }

    public <T> BlockState setValue(BlockState state, Attribute<T> attr, T val) {
        Entry<T> entry = (Entry<T>) mappings[attr.getId()];
        Comparable compVal = entry.codec.toNative(val); // TODO: test; review
        return state.setValue((Property)entry.mapping, compVal);
    }

    public <T> BlockState trySetValue(BlockState state, Attribute<T> attr, T val) {
        try {
            Entry <T> entry = (Entry<T>) mappings[attr.getId()];
            Comparable compVal = entry.codec.toNative(val);
            return state.setValue((Property)entry.mapping, compVal);
        } catch (IllegalArgumentException ex) {
            LOGGER.debug("Falling back to default state ({}); illegal attribute or value", state, ex);
            return state;
        }
    }

    public <T> Entry<T> getEntry(Attribute<T> attr) {
        return (Entry<T>) mappings[attr.getId()];
    }

    public static class DirectionAttrCodec implements AttributeCodec<Direction, net.minecraft.core.Direction> {

        public static final DirectionAttrCodec INST = new DirectionAttrCodec();
        private DirectionAttrCodec() {
            // empty
        }

        @Override
        public Direction fromNative(net.minecraft.core.Direction nType) {
            return switch (nType) {
                case NORTH -> Direction.NORTH;
                case SOUTH -> Direction.SOUTH;
                case EAST -> Direction.EAST;
                case WEST -> Direction.WEST;
                case UP -> Direction.UP;
                case DOWN -> Direction.DOWN;
            };
        }

        @Override
        public net.minecraft.core.Direction toNative(Direction type) {
            return switch (type) {
                case NORTH -> NORTH;
                case SOUTH -> SOUTH;
                case EAST -> EAST;
                case WEST -> WEST;
                case UP -> UP;
                case DOWN -> DOWN;
            };
        }
    }

    public static class AxisAttrCodec implements AttributeCodec<Axis, net.minecraft.core.Direction.Axis> {

        public static final AxisAttrCodec INST = new AxisAttrCodec();
        private AxisAttrCodec() {
            // empty
        }

        @Override
        public Axis fromNative(net.minecraft.core.Direction.Axis nType) {
            return switch (nType) {
                case X -> Axis.X;
                case Y -> Axis.Y;
                case Z -> Axis.Z;
            };
        }

        @Override
        public net.minecraft.core.Direction.Axis toNative(Axis type) {
            return switch (type) {
                case X -> X;
                case Y -> Y;
                case Z -> Z;
            };
        }
    }

    static class BoolAttributeCodec implements AttributeCodec<Boolean, Boolean> {

        public static final AttributeCodec<Boolean, Boolean> INST = new BoolAttributeCodec();

        @Override
        public Boolean fromNative(Boolean nType) {
            return nType;
        }

        @Override
        public Boolean toNative(Boolean type) {
            return type;
        }
    }

    public static class AttributeLookup {

        private int[] propId2EntryLookup;
        private int size;

        public AttributeLookup(int props) {
            this.propId2EntryLookup = new int[props];
            this.size = 0;
        }

        // Return index of entry in propId2EntryLookup, or negated index for insertion if not found
        private int findIndex(int propId) {
            int low = 0;
            int high = size - 1;
            while(low <= high) {
                int mid = (high - low) / 2;
                if(propId2EntryLookup[mid] > propId) {
                    low = mid + 1;
                } else if(propId2EntryLookup[mid] < propId) {
                    high = mid - 1;
                } else {
                    return mid;
                }
            }
            return ~low;
        }

        public void registerPropId(int entryIndex, int propId) {
            int idx = findIndex(propId);
            if(idx < 0) { // if not found, move elements for insertion
                idx = ~idx;
                if(idx < size) {
                    System.arraycopy(propId2EntryLookup, idx, propId2EntryLookup, idx + 1, size - idx);
                }
                size++;
            }
            propId2EntryLookup[idx] = entryIndex;
        }
    }

    public static class Entry<T> {
        public final Attribute<T> attr;
        public final Property<?> mapping;
        public final AttributeCodec<T, ?> codec;
        protected <NT extends Comparable<NT>> Entry(Attribute<T> attr,
                                                       Property<NT> mapping,
                                                       AttributeCodec<T, NT> codec) {
            this.attr = attr;
            this.mapping = mapping;
            this.codec = codec;
        }
    }
}
