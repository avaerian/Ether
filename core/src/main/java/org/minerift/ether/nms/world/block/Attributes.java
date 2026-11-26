package org.minerift.ether.nms.world.block;

import org.minerift.ether.debug.Debug;
import org.minerift.ether.schematic.transform.Axis;
import org.minerift.ether.schematic.transform.Direction;

import static org.minerift.ether.schematic.transform.Direction.*;

public final class Attributes {

    public static final Attribute<?>[] ATTRIBUTES;

    public static final EnumAttribute<Direction> HORIZONTAL_FACING;
    public static final EnumAttribute<Direction> FACING;
    public static final EnumAttribute<Axis> AXIS;
    public static final BooleanAttribute LIT;
    public static final BooleanAttribute FALLING;

    static {
        int i = -1;
        ATTRIBUTES = new Attribute[5];

        ATTRIBUTES[++i] = HORIZONTAL_FACING = EnumAttribute.create(i, "HORIZONTAL_FACING", Direction.class,
                NORTH, SOUTH, EAST, WEST);
        ATTRIBUTES[++i] = FACING = EnumAttribute.create(i, "FACING", Direction.class);
        ATTRIBUTES[++i] = AXIS = EnumAttribute.create(i, "AXIS", Axis.class);
        ATTRIBUTES[++i] = LIT = new BooleanAttribute(i, "LIT");
        ATTRIBUTES[++i] = FALLING = new BooleanAttribute(i, "FALLING");
    }

}
