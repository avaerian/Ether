package org.minerift.ether.database.sql.adapters;

import org.minerift.ether.math.Vec2i;

public class Vec2i2LongAdapter implements Adapter<Vec2i, Long> {

    @Override
    public Long adaptTo(Vec2i obj) {
        return ((long) obj.getX() << 32) | (obj.getZ() & 0xffffffffL);
    }

    @Override
    public Vec2i adaptFrom(Long obj) {
        return new Vec2i((int)(obj >> 32), obj.intValue());
    }
}
