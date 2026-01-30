package org.minerift.ether.world;

import org.minerift.ether.debug.Experimental;
import org.minerift.ether.math.Vec2;
import org.minerift.ether.math.Vec2d;
import org.minerift.ether.math.Vec3;
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

    public Location copy() {
        return new Location(pos.copy(), looking.copy());
    }

    public Location copyImmutable() {
        return new Location(pos.copyImmutable(), looking.copyImmutable());
    }

    public boolean isMutable() {
        return false;
    }

    public Location.Mutable copyMutable() {
        return new Mutable(pos.copyMutable(), looking.copyMutable());
    }

    public Location.Mutable asMutable() {
        return isMutable() ? (Mutable) this : copyMutable();
    }

    public static class Mutable extends Location {

        public Mutable(Vec3d pos, Vec2d looking) {
            super(pos, looking);
        }

        @Override
        public Location copy() {
            return new Location.Mutable(pos.asMutable(), looking.asMutable());
        }

        @Override
        public boolean isMutable() {
            return true;
        }

        public Mutable setPos(Vec3d pos) {
            this.pos = pos;
            return this;
        }

        /**
         * Set the looking direction for this location.
         *
         * @param look new looking direction in the form of x being yaw and z being pitch
         *
         * @return the builder, reflecting the changes made
         */
        public Mutable setLooking(Vec2d look) {
            this.looking = look;
            return this;
        }

        public Mutable addPos(Vec3 addend) {
            this.pos = this.pos.asMutable().add(addend);
            return this;
        }

        public Mutable subtractPos(Vec3 subtrahend) {
            this.pos = this.pos.asMutable().subtract(subtrahend);
            return this;
        }

        public Mutable multiplyPos(Vec3 minuend) {
            this.pos = this.pos.asMutable().multiply(minuend);
            return this;
        }

        public Mutable dividePos(Vec3 diff) {
            this.pos = this.pos.asMutable().divide(diff);
            return this;
        }

        public Mutable addLook(Vec2 addend) {
            this.looking = this.looking.asMutable().add(addend);
            return this;
        }

        public Mutable subtractLook(Vec2 subtrahend) {
            this.looking = this.looking.asMutable().subtract(subtrahend);
            return this;
        }

        public Mutable multiplyLook(Vec2 minuend) {
            this.looking = this.looking.asMutable().multiply(minuend);
            return this;
        }

        public Mutable divideLook(Vec2 diff) {
            this.looking = this.looking.asMutable().divide(diff);
            return this;
        }
    }
}
