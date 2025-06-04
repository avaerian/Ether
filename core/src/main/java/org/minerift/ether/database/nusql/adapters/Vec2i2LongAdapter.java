package org.minerift.ether.database.nusql.adapters;

import org.minerift.ether.math.Maths;
import org.minerift.ether.math.Vec2i;

public class Vec2i2LongAdapter implements Adapter<Vec2i, Long> {

    private static final Maths.PackingOrder ORDER = Maths.PackingOrder.ZX;

    @Override
    public Long adaptTo(Vec2i obj) {
        return Maths.pack(obj, ORDER);
    }

    @Override
    public Vec2i adaptFrom(Long obj) {
        return Maths.unpack(obj, ORDER);
    }
}
