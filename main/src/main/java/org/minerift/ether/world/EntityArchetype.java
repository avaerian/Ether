package org.minerift.ether.world;

import org.minerift.ether.util.math.Vec3d;

public class EntityArchetype {

    private final String id;
    private final Vec3d.Mutable pos;

    public EntityArchetype(String id, Vec3d pos) {
        this.id = id;
        this.pos = pos.asMutable();
    }

    public String getId() {
        return id;
    }

    public Vec3d.Mutable getPos() {
        return pos;
    }

    @Override
    public String toString() {
        return String.format("%s -> %s", pos, id);
    }
}
