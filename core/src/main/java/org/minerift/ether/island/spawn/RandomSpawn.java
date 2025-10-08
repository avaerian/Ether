package org.minerift.ether.island.spawn;

import org.minerift.ether.debug.Experimental;
import org.minerift.ether.debug.NeedsTesting;
import org.minerift.ether.user.EtherUser;
import org.minerift.ether.util.UnreachableException;
import org.minerift.ether.util.nbt.tags.container.CompoundTag;
import org.minerift.ether.world.Location;

import static org.minerift.ether.util.nbt.tags.TagTypes.COMPOUND;

// TODO
@Experimental
@NeedsTesting
public class RandomSpawn extends SpawnStrategy {

    static {
        register(RandomSpawn.class, COMPOUND, RandomSpawn::of);
    }

    public static final String TYPE_NAME = "SPAWN_RANDOM";

    public static RandomSpawn of(CompoundTag nbt) {
        throw new UnreachableException("unimplemented"); // TODO
    }

    @Override
    public Location spawn(EtherUser user) {
        return null;
    }

    @Override
    public String getTypeName() {
        return TYPE_NAME;
    }

    @Override
    public CompoundTag serializeNbt() {
        return null;
    }
}
