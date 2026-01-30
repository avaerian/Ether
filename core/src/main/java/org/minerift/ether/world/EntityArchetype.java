package org.minerift.ether.world;

import org.minerift.ether.math.Vec3d;
import org.minerift.ether.util.nbt.tags.container.CompoundTag;

public class EntityArchetype {

    private final String id;
    private final Location.Mutable loc;
    private final CompoundTag nbtData;

    public EntityArchetype(String id, Location loc, CompoundTag nbtData) {
        this.id = id;
        this.loc = loc.asMutable();
        this.nbtData = nbtData;
    }

    public String getId() {
        return id;
    }

    public Location.Mutable getLocation() {
        return loc;
    }

    public CompoundTag getNbtData() {
        return nbtData;
    }

    @Override
    public String toString() {
        return String.format("%s -> %s", loc, id);
    }
}
