package org.minerift.ether.island.spawn;

import org.minerift.ether.math.*;
import org.minerift.ether.user.EtherUser;
import org.minerift.ether.util.nbt.tags.container.CompoundTag;
import org.minerift.ether.world.Location;

import static org.minerift.ether.util.nbt.tags.TagTypes.COMPOUND;

public class SingleSpawn extends SpawnStrategy {

    static {
        register(SingleSpawn.class, COMPOUND, SingleSpawn::of);
    }

    public static final String TYPE_NAME = "SPAWN_SINGLE";

    public static SingleSpawn of(Vec3 pos) {
        return new SingleSpawn(pos.asVec3d(), Vec2d.ZERO); // TODO: review ZERO default
    }

    public static SingleSpawn of(Vec3 pos, Vec2d yawPitch) {
        return new SingleSpawn(pos.asVec3d(), yawPitch);
    }

    public static SingleSpawn of(Location loc) {
        return new SingleSpawn(loc);
    }

    public static SingleSpawn of(CompoundTag tag) throws SpawnStrategyLoadException {
        try {
            return new SingleSpawn(Location.of(tag));
        } catch (IllegalArgumentException e) {
            throw new SpawnStrategyLoadException(e);
        }
    }

    private final Location loc;

    public SingleSpawn(Vec3 pos, Vec2d yawPitch) {
        this.loc = new Location(pos.asVec3d(), yawPitch);
    }

    public SingleSpawn(Location loc) {
        this.loc = loc;
    }

    @Override
    public Location spawn(EtherUser user) {
        //user.getOfflinePlayer().getPlayer().teleportAsync();
        return loc;
    }

    @Override
    public String getTypeName() {
        return TYPE_NAME;
    }

    public Vec3d getSpawnPos() {
        return loc.pos();
    }

    public Vec2d getSpawnLooking() {
        return loc.looking();
    }

    public Location getSpawnLocation() {
        return loc;
    }

    @Override
    public CompoundTag serializeNbt() {
        CompoundTag tag = loc.serializeNbt(); // may add additional info later
        return tag;
    }
}
