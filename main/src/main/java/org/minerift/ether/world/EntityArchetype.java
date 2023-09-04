package org.minerift.ether.world;

import org.minerift.ether.util.math.Vec3d;
import org.minerift.ether.util.nbt.tags.CompoundTag;

public class EntityArchetype {

    private final String type;
    private final Vec3d.Mutable pos;
    private final CompoundTag nbtData;

    public EntityArchetype(String type, Vec3d pos, CompoundTag nbtData) {
        this.type = type;
        this.pos = pos.asMutable();
        this.nbtData = nbtData;
    }

    public String getType() {
        return type;
    }

    public Vec3d.Mutable getPos() {
        return pos;
    }

    public CompoundTag getNbtData() {
        return nbtData;
    }

    @Override
    public String toString() {
        return String.format("%s -> %s", pos, type);
    }
}
