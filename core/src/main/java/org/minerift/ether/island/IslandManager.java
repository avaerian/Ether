package org.minerift.ether.island;

import org.bukkit.Location;
import org.bukkit.World;
import org.minerift.ether.Ether;
import org.minerift.ether.config.ConfigType;
import org.minerift.ether.config.islandspecs.IslandSpec;
import org.minerift.ether.config.main.MainConfig;
import org.minerift.ether.math.Vec2i;
import org.minerift.ether.math.Vec3i;
import org.minerift.ether.schematic.Schematic;
import org.minerift.ether.schematic.SchematicPasteOptions;
import org.minerift.ether.user.EtherUser;
import org.minerift.ether.util.BukkitUtils;

import java.util.*;

public class IslandManager {

    private IslandGrid grid;

    public IslandManager() {
        this(new DefaultIslandGrid());
    }

    public IslandManager(IslandGrid grid) {
        this.grid = grid;
    }

    public Set<Integer> getKeySet() {
        var data = ((DefaultIslandGrid)grid).getData(); // be weary of this
        Set<Integer> keys = new HashSet<>(data.size());
        for(Island island : data) {
            // Return only active island ids for key set
            if(island != null && !island.isDeleted()) {
                keys.add(island.getId());
            }
        }
        return keys;
    }

    // Get multiple islands
    // Returns active, deleted, and null islands
    public List<Island> getIslands(Collection<Integer> ids) {
        List<Island> islands = ids.isEmpty() ? Collections.emptyList() : new ArrayList<>(ids.size());
        for(int id : ids) {
            islands.add(grid.getIslandAt(id).orElse(null));
        }
        return islands;
    }

    @Deprecated(forRemoval = true)
    public Island createIsland(EtherUser user) {
        if(user.getIsland() != null) {
            // TODO: logger
        }
        return IslandCreationRoutine.run(grid, user);
    }

    @Deprecated public static final int TEMP_ISLAND_HEIGHT = 90;
    @SuppressWarnings("Duplicates") // FIXME: temp until delete IslandCreationRoutine
    // creates island data, places island in world, and updates user island refs
    public Island createIsland(World world, IslandSpec spec, EtherUser owner) {

        MainConfig cfg = Ether.inst().getConfig(ConfigType.MAIN);

        final Vec2i tile = grid.getNextTile();
        System.out.println("Tile = " + tile);
        final Vec2i blChunk = new Vec2i(tile.getX() * cfg.getTileLengthChunks(), tile.getZ() * cfg.getTileLengthChunks());
        final Vec2i trChunk = new Vec2i((cfg.getTileLengthChunks() * (tile.getX() + 1)) - 1, (cfg.getTileLengthChunks() * (tile.getZ() + 1)) - 1);

        System.out.println("blChunk = " + blChunk);
        System.out.println("trChunk = " + trChunk);

        // TODO: check if island hasn't been purged at current tile

        // TODO: get center chunk, get center in chunk, and get offset for pasting

        Vec2i centerChunk = new Vec2i(
                blChunk.getX() + (cfg.getTileLengthChunks() / 2),
                blChunk.getZ() + (cfg.getTileLengthChunks() / 2));

        // TODO: for different dimensions, allow for individual island heights
        Vec3i centerBlock = new Vec3i((centerChunk.getX() * 16) - 8, TEMP_ISLAND_HEIGHT, (centerChunk.getZ() * 16) - 8);
        Schematic schem = spec.getSchematic();

        // center of schematic struct
        Vec3i offset = new Vec3i(-schem.getWidth() / 2, -schem.getHeight() / 2, -schem.getLength() / 2);
        SchematicPasteOptions options = SchematicPasteOptions.builder()
                .copyBiomes(false) // TODO: review; will set manually?
                .copyEntities(true)
                .setOffset(offset)
                .build();

        schem.paste(centerBlock, world.getName(), options);

        Island.Builder builder = Island.builder()
                .setOwner(owner)
                .setTile(tile, true)
                .setBottomLeftBound(blChunk)
                .setTopRightBound(trChunk)
                .setDeleted(false);

        //new PermissionSet().set();

        return builder.build();
    }

    public void deleteIsland(Island island) { // TODO

        // Mark island as deleted
        island.markDeleted();

        // Clear all island information (?)

        // Remove all entities in world within island region
        // Scan island and clear/set to air
        // Remove island references from players on island team
        island.getTeamMembers().forEach(island::removeTeamMember);
        // Teleport players back to spawn
    }

    public Optional<Island> getIslandAt(Vec2i tile) {
        return grid.getIslandAt(tile);
    }

    public Optional<Island> getIslandAt(Integer islandId) {
        return grid.getIslandAt(islandId);
    }

    public Optional<Island> getIslandAt(Location location) {
        return getIslandAt(BukkitUtils.getTileAt(location));
    }
}
