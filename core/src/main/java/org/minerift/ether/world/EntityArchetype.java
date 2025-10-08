package org.minerift.ether.world;

import org.minerift.ether.math.Vec3d;
import org.minerift.ether.util.nbt.tags.container.CompoundTag;

public class EntityArchetype {

    private final String id;
    private final Vec3d.Mutable pos;
    private final CompoundTag nbtData;

    public EntityArchetype(String id, Vec3d pos, CompoundTag nbtData) {
        this.id = id;
        this.pos = pos.asMutable();
        this.nbtData = nbtData;
    }

    public String getId() {
        return id;
    }

    public Vec3d.Mutable getPos() {
        return pos;
    }

    public CompoundTag getNbtData() {
        return nbtData;
    }

    @Override
    public String toString() {
        return String.format("%s -> %s", pos, id);
    }
}
