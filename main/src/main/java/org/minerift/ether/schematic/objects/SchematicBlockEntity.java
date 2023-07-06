package org.minerift.ether.schematic.objects;

import org.minerift.ether.util.math.Vec3i;

import java.util.Map;

public class SchematicBlockEntity {

    private final String id;
    private final Vec3i pos;
    private final Map<String, Object> extra;

    public SchematicBlockEntity(String id, Vec3i pos, Map<String, Object> extra) {
        this.id = id;
        this.pos = pos;
        this.extra = extra;
    }

    public String getId() {
        return id;
    }

    public Vec3i getPos() {
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
