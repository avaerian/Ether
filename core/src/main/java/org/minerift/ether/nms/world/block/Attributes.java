package org.minerift.ether.nms.world.block;

import org.minerift.ether.debug.Debug;
import org.minerift.ether.schematic.transform.Axis;
import org.minerift.ether.schematic.transform.Direction;

public final class Attributes {

    public static final Attribute<?>[] ATTRIBUTES;

    public static final EnumAttribute<Direction> HORIZONTAL_FACING;
    public static final EnumAttribute<Direction> FACING;
    public static final EnumAttribute<Axis> AXIS;

    static {
        int i = -1;
        ATTRIBUTES = new Attribute[3];

        ATTRIBUTES[++i] = HORIZONTAL_FACING = EnumAttribute.create(i, "HORIZONTAL_FACING", Direction.class);
        ATTRIBUTES[++i] = FACING = EnumAttribute.create(i, "FACING", Direction.class);
        ATTRIBUTES[++i] = AXIS = EnumAttribute.create(i, "AXIS", Axis.class);
    }

    @Debug
    public static void main(String[] args) {
        System.out.println("hello, world");
    }


}
