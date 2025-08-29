package org.minerift.ether.schematic.transform;

import org.minerift.ether.debug.Debug;
import org.minerift.ether.math.Vec3i;
import org.minerift.ether.util.Option;

import static org.minerift.ether.schematic.transform.Rotate.Angle.DG_90;
import static org.minerift.ether.schematic.transform.Rotate.Angle.DG_NEG_90;

public enum Direction {

    NORTH(1,0,0), // 1,0,0
    SOUTH(-1,0,0), // -1,0,0
    EAST(0,0,1), // Vector 0,0,1
    WEST(0,0,-1), // Vector 0,0,-1
    UP(0,1,0), // Vector 0,1,0
    DOWN(0,-1,0), // Vector 0,-1,0

    ;

    /*public static final int KEY_NORTH = key(NORTH);
    public static final int KEY_SOUTH = key(SOUTH);
    public static final int KEY_EAST = key(EAST);
    public static final int KEY_WEST = key(WEST);
    public static final int KEY_UP = key(UP);
    public static final int KEY_DOWN = key(DOWN);*/
    public static final Vec3i ROT_MATRIX_SIZE = new Vec3i(3,3,3);

    public static final int HORIZONTAL = 1;
    public static final int VERTICAL = 2;
    public static final int ALL_PLANES = HORIZONTAL | VERTICAL;

    private final Vec3i vec;

    Direction(int x, int y, int z) {
        this(new Vec3i(x,y,z));
    }

    @Debug
    public static void main(String[] args) {
        System.out.println(Integer.toBinaryString(0xFF << 24));
        for(Direction d : Direction.values()) {
            System.out.println(d + " " + d.vec + " -> " + Direction.fromVector(d.vec) + " -> " + d.getClockwise());
        }
    }

    Direction(Vec3i vec) {
        this.vec = vec;
        System.out.println(this + " " + Integer.toBinaryString(key(this)));
    }

    public Vec3i getNormal() {
        return vec;
    }

    // TODO: cache clockwise and counter-clockwise rotations?
    public Direction getClockwise() {
        return getClockwise(Axis.Y);
    }

    public Direction getClockwise(Axis axis) {
        return rotate(axis, DG_90);
    }

    public Direction getCounterClockwise() {
        return getCounterClockwise(Axis.Y);
    }

    public Direction getCounterClockwise(Axis axis) {
        return rotate(axis, DG_NEG_90);
    }

    // cache?
    public boolean facingHorizontal() {
        return switch (this) {
            case NORTH, SOUTH, EAST, WEST -> true;
            case UP, DOWN -> false;
        };
    }

    public boolean facingVertical() {
        return !facingHorizontal();
    }

    public Axis getAxis() {
        return switch (this) {
            case NORTH, SOUTH -> Axis.X;
            case UP, DOWN -> Axis.Y;
            case EAST, WEST -> Axis.Z;
        };
    }

    private static int key(Vec3i vec) {
        return ((vec.getX() & 0xFF) << 16)
                | ((vec.getY() & 0xFF) << 8)
                | (vec.getZ() & 0xFF);
    }

    private static int key(Direction d) {
        return key(d.vec);
    }

    // TODO: create fn for non-normalized vecs
    public static Direction fromVector(Vec3i vec) {
        int key = key(vec);
        return switch (key) {
            case (1 << 16 /*| 0 << 8 | 0*/) -> NORTH;
            case (0xFF << 16 /*| 0 << 8 | 0*/) -> SOUTH;
            case (1) -> EAST;
            case (0xFF) -> WEST;
            case (1 << 8) -> UP;
            case (0xFF << 8) -> DOWN;
            default -> throw new IllegalStateException("Unexpected value: " + key);
        };
    }

    public Direction rotate(Axis axis, Rotate.Angle angle, int flags) {
        if( flags == ALL_PLANES
                || ((flags & HORIZONTAL) != 0 && axis.rotatesHorizontalPlane())
                || ((flags & VERTICAL) != 0 && axis.rotatesVerticalPlane()) ) {
            return rotate(axis, angle);
        } else {
            return this;
        }
    }

    public Direction rotate(Axis axis, Option<Rotate.Angle> angle, int flags) {
        return switch (angle) {
            case Option.Some<Rotate.Angle> s -> rotate(axis, angle, flags);
            case Option.None<Rotate.Angle> n -> this;
            default -> throw new IllegalStateException("Unexpected value: " + angle);
        };
    }

    public Direction rotate(Axis axis, Rotate.Angle angle) {
        Vec3i.Mutable gridLoc = vec.asMutableCopy().add(1, 1, 1);
        Vec3i rotated = Rotate.transformVec(axis, angle, ROT_MATRIX_SIZE, gridLoc).out;
        return fromVector(rotated.asMutableCopy().subtract(1,1,1));
    }
}
