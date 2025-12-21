package org.minerift.ether.world;

import org.minerift.ether.debug.Experimental;
import org.minerift.ether.math.Vec2d;
import org.minerift.ether.math.Vec3d;
import org.minerift.ether.util.nbt.NbtSerializable;
import org.minerift.ether.util.nbt.nunbt.DoubleArrayNuTag;
import org.minerift.ether.util.nbt.transmute.UnwrapTags;
import org.minerift.ether.util.nbt.tags.Tag;
import org.minerift.ether.util.nbt.tags.container.CompoundTag;
import org.minerift.ether.util.nbt.tags.container.ListTag;

import static org.minerift.ether.util.nbt.nunbt.DoubleArrayNuTag.Codec.tagsToDoubles;
import static org.minerift.ether.util.nbt.nunbt.NuTagTypes.DOUBLE_ARRAY;
import static org.minerift.ether.util.nbt.tags.TagTypes.DOUBLE;
import static org.minerift.ether.util.nbt.tags.TagTypes.LIST;

public class Location implements NbtSerializable {

    public static final String POS_ENTRY = "position"; // double[]
    public static final String LOOK_ENTRY = "looking"; // double[]

    @Experimental public static final UnwrapTags<double[]> DOUBLE_LIST_OR_ARRAY;

    static {
        DOUBLE_LIST_OR_ARRAY = UnwrapTags.to(double[].class)
                .add(ListTag.class, (t) -> t.is(LIST) && ((ListTag<?>)t).childTypeIs(DOUBLE),
                        (t) -> tagsToDoubles(t.withChildType(DOUBLE).getValue()))
                .add(DoubleArrayNuTag.class, (t) -> t.is(DOUBLE_ARRAY), DoubleArrayNuTag::getValue)
                .immutable();
    }

    public static Location of(CompoundTag tag) throws IllegalArgumentException {
        Tag posTag = tag.getTag(POS_ENTRY, (e) -> new IllegalArgumentException("Failed to find position", e));
        Tag lookTag = tag.getTag(LOOK_ENTRY, (e) -> new IllegalArgumentException("Failed to find looking", e));

        double[] pos = DOUBLE_LIST_OR_ARRAY.unwrap(posTag,
                (e) -> new IllegalArgumentException("Failed to read position", e));
        double[] look = DOUBLE_LIST_OR_ARRAY.unwrap(lookTag,
                (e) -> new IllegalArgumentException("Failed to read looking", e));

        return new Location(new Vec3d(pos), new Vec2d(look));
    }

    protected Vec3d pos;
    protected Vec2d looking;

    public Location(Vec3d pos, Vec2d looking) {
        this.pos = pos;
        this.looking = looking;
    }

    public int getX() {
        return pos.getX();
    }

    public int getY() {
        return pos.getY();
    }

    public int getZ() {
        return pos.getZ();
    }

    public double getXd() {
        return pos.getXd();
    }

    public double getYd() {
        return pos.getYd();
    }

    public double getZd() {
        return pos.getZd();
    }

    public double getYaw() {
        return looking.getXd();
    }

    public double getPitch() {
        return looking.getZd();
    }

    public Vec3d pos() {
        return pos;
    }

    public Vec2d looking() {
        return looking;
    }

    @Override
    public CompoundTag serializeNbt() {
        CompoundTag tag = new CompoundTag();
        tag.addTag( new DoubleArrayNuTag(POS_ENTRY, pos.getXYZd()) );
        tag.addTag( new DoubleArrayNuTag(LOOK_ENTRY, looking.getXZd() ));
        return tag;
    }
}
