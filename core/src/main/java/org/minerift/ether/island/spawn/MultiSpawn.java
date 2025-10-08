package org.minerift.ether.island.spawn;

import com.google.common.base.Preconditions;
import org.minerift.ether.user.EtherUser;
import org.minerift.ether.util.nbt.tags.container.CompoundTag;
import org.minerift.ether.util.nbt.tags.container.ListTag;
import org.minerift.ether.world.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.minerift.ether.util.nbt.tags.TagTypes.COMPOUND;

public class MultiSpawn extends SpawnStrategy {

    static {
        register(MultiSpawn.class, COMPOUND, MultiSpawn::of);
    }

    public static final Random RANDOM = new Random();
    public static final String TYPE_NAME = "SPAWN_MULTI";
    public static final String LOCS_ENTRY = "locations";

    public static MultiSpawn of(List<Location> locs) {
        Preconditions.checkNotNull(locs);
        Preconditions.checkArgument(!locs.isEmpty(), "Must provide at least one location entry");
        return new MultiSpawn(locs.toArray(Location[]::new));
    }

    public static MultiSpawn of(Location[] locs) {
        Preconditions.checkNotNull(locs);
        Preconditions.checkArgument(locs.length > 0, "Must provide at least one location entry");
        return new MultiSpawn(locs);
    }

    public static MultiSpawn of(CompoundTag root) throws SpawnStrategyLoadException {
        ListTag<CompoundTag> locTags = root.getList(LOCS_ENTRY, COMPOUND,
                (e) -> new SpawnStrategyLoadException("Failed to read locations", e));
        Location[] locs = new Location[locTags.size()];
        try {
            int i = 0;
            for(CompoundTag loc : locTags) {
                locs[i++] = Location.of(loc);
            }
        } catch (IllegalArgumentException e) { // if one location fails, fail all (???????)
            throw new SpawnStrategyLoadException("Failed to read location entry", e);
        }
        return new MultiSpawn(locs);
    }

    private final Location[] locs;

    public MultiSpawn(Location[] locs) {
        this.locs = locs;
    }

    @Override
    public Location spawn(EtherUser user) {
        return spawn(user, RANDOM);
    }

    public Location spawn(EtherUser user, Random rng) {
        return locs[ rng.nextInt(locs.length) ];
    }

    @Override
    public String getTypeName() {
        return TYPE_NAME;
    }

    @Override
    public CompoundTag serializeNbt() {
        CompoundTag root = new CompoundTag();
        ListTag<CompoundTag> locTags = new ListTag<>(LOCS_ENTRY, COMPOUND, new ArrayList<>(locs.length));
        for(Location loc : locs) {
            locTags.addTag(loc.serializeNbt());
        }
        root.addTag( locTags );
        return root;
    }
}
