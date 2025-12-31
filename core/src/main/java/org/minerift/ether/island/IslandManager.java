package org.minerift.ether.island;

import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.bukkit.Location;
import org.bukkit.World;
import org.minerift.ether.Ether;
import org.minerift.ether.config.ConfigType;
import org.minerift.ether.config.islandspecs.IslandSpec;
import org.minerift.ether.config.main.MainConfig;
import org.minerift.ether.debug.Debug;
import org.minerift.ether.math.Vec2i;
import org.minerift.ether.math.Vec3i;
import org.minerift.ether.nms.NMSAccess;
import org.minerift.ether.nms.world.Chunk;
import org.minerift.ether.nms.world.ChunkGetter;
import org.minerift.ether.schematic.Schematic;
import org.minerift.ether.schematic.SchematicPasteOptions;
import org.minerift.ether.user.EtherUser;
import org.minerift.ether.util.BukkitUtils;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class IslandManager {

    private final IslandGrid grid;

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

    // TODO: add options parameter for additional island creation config (e.g. ChunkGetter)
    // Creates island data, places island in world, and updates user island refs
    public CompletableFuture<Island> createIsland(World world, IslandSpec spec, EtherUser owner) {

        owner.getPlayer().orElseThrow(() -> new IllegalArgumentException("User must be online to create island"));
        MainConfig cfg = Ether.inst().getConfig(ConfigType.MAIN);

        final Vec2i tile = grid.getNextTile();
        final Vec2i blChunk = new Vec2i(tile.getX() * cfg.getTileLengthChunks(), tile.getZ() * cfg.getTileLengthChunks());
        final Vec2i trChunk = new Vec2i((cfg.getTileLengthChunks() * (tile.getX() + 1)) - 1, (cfg.getTileLengthChunks() * (tile.getZ() + 1)) - 1);

        /*System.out.println("Tile = " + tile);
        System.out.println("blChunk = " + blChunk);
        System.out.println("trChunk = " + trChunk);*/

        Vec2i centerChunk = new Vec2i(
                blChunk.getX() + (cfg.getTileLengthChunks() / 2),
                blChunk.getZ() + (cfg.getTileLengthChunks() / 2));

        // TODO: for different dimensions, allow for individual island heights; add this in main cfg
        Vec3i centerBlock = new Vec3i((centerChunk.getX() * 16) - 8, cfg.getTileHeight(), (centerChunk.getZ() * 16) - 8);
        Schematic schem = spec.getSchematic();

        Vec3i offset = new Vec3i(-schem.getWidth() / 2, -schem.getHeight() / 2, -schem.getLength() / 2);
        SchematicPasteOptions options = SchematicPasteOptions.builder()
                .copyBiomes(false)
                .copyEntities(true)
                .setOffset(offset)
                .build();

        @Debug final ChunkGetter cg = ChunkGetter.ASYNC;
        final IslandGrid syncGrid = IslandGrid.synchronize(grid);

        // seems messy; we'll see if there's a better solution
        CompletableFuture<Void> cf = CompletableFuture.runAsync(() -> {
            final NMSAccess nms = Ether.inst().getNms();
            if(syncGrid.needsClearing(tile)) {
                CompletableFuture<Void>[] futures =
                        new CompletableFuture[(trChunk.getX() - blChunk.getX()) * (trChunk.getZ() - blChunk.getZ())];
                for(int z = blChunk.getZ(), i = 0; z < trChunk.getZ(); z++) {
                    for(int x = blChunk.getX(); x < trChunk.getX(); x++) {
                        futures[i++] = cg.getChunk(world, x, z)
                                .thenAccept((chunk) -> nms.clearChunk(chunk, true));
                    }
                }
                CompletableFuture.allOf(futures).join();
            }
            schem.paste(centerBlock, world.getName(), options);
        });

        return cf.thenApply((__) -> { // FIXME: may need to schedule via WorkQueue for main thread
            Island.Builder builder = Island.builder()
                    .setOwner(owner)
                    .setTile(tile, true)
                    .setBottomLeftBound(blChunk)
                    .setTopRightBound(trChunk)
                    .setDeleted(false);

            final Island island = builder.build();
            syncGrid.registerIsland(island);
            owner.getPlayer().get().teleportAsync(BukkitUtils.asLocation(world, centerBlock));
            return island;
        });
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
