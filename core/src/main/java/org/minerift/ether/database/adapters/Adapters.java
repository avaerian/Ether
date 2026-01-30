package org.minerift.ether.database.adapters;

import org.minerift.ether.math.Vec2i;

public class Adapters {
    public static final EtherUsers2UuidsAdapter ETHER_USERS_2_UUIDS = new EtherUsers2UuidsAdapter();
    public static final Vec2i2LongAdapter VEC2I_2_LONG = new Vec2i2LongAdapter();
    public static final Vec2is2LongsAdapter VEC2IS_2_LONGS = new Vec2is2LongsAdapter();
    public static final Pair2UuidsAdapter PAIR_2_UUIDS = new Pair2UuidsAdapter();

}
