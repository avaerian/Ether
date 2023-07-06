package org.minerift.ether.schematic.objects;

import org.minerift.ether.util.math.Vec3d;

import java.util.Map;

public class SchematicEntity {
    private final String id;
    private final Vec3d pos;
    private final Map<String, Object> extra;

    public SchematicEntity(String id, Vec3d pos, Map<String, Object> extra) {
        this.id = id;
        this.pos = pos;
        this.extra = extra;
    }

    public String getId() {
        return id;
    }

    public Vec3d getPos() {
        return pos;
    }

    public Map<String, Object> getExtra() {
        return extra;
    }

    @Override
    public String toString() {
        return String.format("id: %s, pos: %s, extra: %s", id, pos, extra);
    }
}
