package org.minerift.ether.island;

import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitTask;
import org.minerift.ether.Ether;
import org.minerift.ether.config.ConfigType;
import org.minerift.ether.config.islandspecs.IslandSpec;
import org.minerift.ether.config.main.MainConfig;
import org.minerift.ether.math.Vec2i;
import org.minerift.ether.math.Vec3i;
import org.minerift.ether.nms.world.Chunk;
import org.minerift.ether.nms.world.ChunkGetter;
import org.minerift.ether.schematic.Schematic;
import org.minerift.ether.schematic.SchematicPasteOptions;
import org.minerift.ether.user.EtherUser;
import org.minerift.ether.util.BukkitUtils;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class IslandManager {

    private IslandGrid grid;
    public IslandManager(IslandGrid grid) {
        this.grid = grid;
    }

    public IslandManager() {
        this.grid = new DefaultIslandGrid();
    }

    public IntSet getKeySet() {
        return grid.getIslandIds();
    }

    // Get multiple islands
    // Returns active and null islands
    public List<Island> getIslands(IntCollection ids) {
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
        return IslandCreationRoutine.run(null, user);
    }

    @Deprecated public static final int TEMP_ISLAND_HEIGHT = 90;
    @SuppressWarnings("Duplicates") // temp until delete IslandCreationRoutine
    // Creates island data, places island in world, and updates user island refs
    public CompletableFuture<Island> createIsland(World world, IslandSpec spec, EtherUser owner) {
        MainConfig cfg = Ether.inst().getConfig(ConfigType.MAIN);

        final Vec2i tile = grid.getNextTile();
        System.out.println("Tile = " + tile);
        final Vec2i blChunk = new Vec2i(tile.getX() * cfg.getTileLengthChunks(), tile.getZ() * cfg.getTileLengthChunks());
        final Vec2i trChunk = new Vec2i((cfg.getTileLengthChunks() * (tile.getX() + 1)) - 1, (cfg.getTileLengthChunks() * (tile.getZ() + 1)) - 1);

        System.out.println("blChunk = " + blChunk);
        System.out.println("trChunk = " + trChunk);

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

        // TODO: schedule clearing and pasting on another thread,
        //  then return island in CompletableFuture
        CompletableFuture<Void> cf = null;
        if(grid.needsClearing(tile)) {
            Chunk bl = Chunk.of(world, blChunk.getX(), blChunk.getZ()).join();
            Chunk tr = Chunk.of(world, trChunk.getX(), trChunk.getZ()).join();
            cf = Ether.inst().getNms().clearChunks(ChunkGetter.ASYNC, bl, tr, true);
            // Iterate through chunks between bl and tr, synchronize and clear
            // This will be a CompletableFuture#allOf to ensure chunks
            // complete sync/async together w/out waiting for each chunk
        }
        if(cf == null) {
            cf = CompletableFuture.runAsync(() -> schem.paste(centerBlock, world.getName(), options));
        }

        // Experimental stuff to explore concurrency
        //CompletableFuture.runAsync(() -> {}).thenApply((__) -> Island.builder().build()).join();

        Island.Builder builder = Island.builder()
                .setOwner(owner)
                .setTile(tile, true)
                .setBottomLeftBound(blChunk)
                .setTopRightBound(trChunk)
                .setDeleted(false);

        final Island island = builder.build();
        grid.registerIsland(island);
        return CompletableFuture.completedFuture(island); // FIXME: switch once scheduling is done
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
