package org.minerift.ether.island;

import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.bukkit.Location;
import org.bukkit.World;
import org.minerift.ether.Ether;
import org.minerift.ether.config.ConfigType;
import org.minerift.ether.config.islandspecs.IslandSpec;
import org.minerift.ether.config.main.MainConfig;
import org.minerift.ether.dimension.Dimension;
import org.minerift.ether.math.Vec2i;
import org.minerift.ether.math.Vec3i;
import org.minerift.ether.nms.NMSAccess;
import org.minerift.ether.nms.world.ChunkGetter;
import org.minerift.ether.schematic.Schematic;
import org.minerift.ether.schematic.SchematicPasteOptions;
import org.minerift.ether.user.EtherUser;
import org.minerift.ether.util.BukkitUtils;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class IslandManager {

    private final IslandGrid grid;

    private long nextClearTimestamp;

    public IslandManager(IslandGrid grid, int timeUntilNextPurgeSecs) {
        this.grid = grid;
        this.nextClearTimestamp += System.currentTimeMillis()
                + TimeUnit.SECONDS.toMillis(timeUntilNextPurgeSecs);
        // TODO: create task for checking if islands needs to be cleared
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
        final Dimension dim = Dimension.from(world);

        final Vec2i tile = grid.getNextTile();
        final Vec2i blChunk = Island.getBottomLeftBound(dim, tile);
        final Vec2i trChunk = Island.getTopRightBound(dim, tile);

        Vec2i centerChunk = new Vec2i(
                blChunk.getX() + (dim.getTileLenChunks() / 2),
                blChunk.getZ() + (dim.getTileLenChunks() / 2));

        Vec3i centerBlock = new Vec3i((centerChunk.getX() * 16) - 8, dim.getIslandSpawnY(), (centerChunk.getZ() * 16) - 8);
        Schematic schem = spec.getSchematic();

        Vec3i offset = new Vec3i(-schem.getWidth() / 2, -schem.getHeight() / 2, -schem.getLength() / 2);
        SchematicPasteOptions options = SchematicPasteOptions.builder()
                .copyBiomes(false)
                .copyEntities(true)
                .setOffset(offset)
                .build();

        final IslandGrid syncGrid = IslandGrid.synchronize(grid, grid);

        CompletableFuture<Void> cf = CompletableFuture.runAsync(() -> {
            final NMSAccess nms = Ether.inst().getNms();
            if(syncGrid.needsClearing(tile)) {
                CompletableFuture<Void>[] chunkTasks =
                        new CompletableFuture[(trChunk.getX() - blChunk.getX()) * (trChunk.getZ() - blChunk.getZ())];
                for(int z = blChunk.getZ(), i = 0; z < trChunk.getZ(); z++) {
                    for(int x = blChunk.getX(); x < trChunk.getX(); x++) {
                        chunkTasks[i++] = ChunkGetter.ASYNC.getChunk(world, x, z)
                                .thenAcceptAsync((chunk) -> nms.clearChunk(chunk, true));
                    }
                }
                CompletableFuture.allOf(chunkTasks).join();
            }
            schem.paste(centerBlock, world.getName(), options); // this needs to be blocking
        });

        return cf.thenApply((__) -> { // FIXME: may need to schedule via WorkQueue for main thread
            Island.Builder builder = Island.builder()
                    .setOwner(owner)
                    .setTile(tile, true)
                    .setBottomLeftBound(blChunk)
                    .setTopRightBound(trChunk);

            final Island island = builder.build();
            syncGrid.registerIsland(island);

            owner.setIsland(island);

            owner.getPlayer().orElseThrow(() -> new IllegalStateException("User offline before island creation completed"))
                    .teleportAsync(BukkitUtils.asBukkitLocation(world, centerBlock));
            return island;
        });
    }

    public CompletableFuture<Vec2i> deleteIsland(Island island, ChunkGetter cg) {

        final MainConfig cfg = Ether.inst().getConfig(ConfigType.MAIN);
        final CompletableFuture<Vec2i> cf;
        switch(cfg.purgeIslandsOption()) {
            case THRESHOLD -> {
                grid.queueForClearing(island.getTile());
                final Collection<Vec2i> queue = Collections.synchronizedCollection(grid.getPurgeQueue());
                CompletableFuture<Vec2i>[] clearAllTilesFs = new CompletableFuture[queue.size()];
                int i = 0;
                if(queue.size() >= cfg.getPurgedIslandsThreshold()) {
                    for(Vec2i tile : queue) {
                        for(Dimension dim : cfg.getDimensions().values()) {
                            clearAllTilesFs[i++] = clearTile(dim, tile, cg).thenApply((tile0) -> {
                                queue.remove(tile0);
                                return tile0;
                            });
                        }
                    }
                }
                cf = CompletableFuture.allOf(clearAllTilesFs).thenApply((__) -> island.getTile());
            }

            case QUEUED -> {
                grid.queueForClearing(island.getTile());
                cf = CompletableFuture.completedFuture(island.getTile());
            }

            case INSTANT -> {
                CompletableFuture<Void>[] clearTileFs = new CompletableFuture[cfg.getDimensions().size()];
                int i = 0;
                for(Dimension dim : cfg.getDimensions().values()) {
                    clearTileFs[i++] = clearTile(dim, island.getTile(), cg).thenAccept((tile) -> {
                        Collections.synchronizedCollection(grid.getPurgeQueue()).remove(tile);
                    });
                }
                cf = CompletableFuture.allOf(clearTileFs).thenApply((__) -> island.getTile());
            }

            default -> throw new IllegalStateException("Unexpected value: " + cfg.purgeIslandsOption());
        }

        grid.unregisterIsland(island);

        // Remove island references from players on island team
        island.getTeamMembers().forEach(island::removeTeamMember);
        // Teleport players back to spawn

        return cf;
    }

    public CompletableFuture<Vec2i> deleteIsland(int islandId, ChunkGetter cg) {
        return deleteIsland(grid.getIslandAt(islandId).orElseThrow(), cg);
    }

    public CompletableFuture<Vec2i> deleteIsland(Vec2i tile, ChunkGetter cg) {
        return deleteIsland(grid.getIslandAt(tile).orElseThrow(), cg);
    }

    public CompletableFuture<Vec2i> deleteIsland(Island island) {
        return deleteIsland(island, ChunkGetter.SYNC);
    }

    public CompletableFuture<Vec2i> deleteIsland(int islandId) {
        return deleteIsland(grid.getIslandAt(islandId).orElseThrow(), ChunkGetter.SYNC);
    }

    public CompletableFuture<Vec2i> deleteIsland(Vec2i tile) {
        return deleteIsland(grid.getIslandAt(tile).orElseThrow(), ChunkGetter.SYNC);
    }

    public CompletableFuture<Vec2i> clearTile(Dimension dim, Vec2i tile, ChunkGetter cg) {
        Vec2i trChunk = Island.getTopRightBound(dim, tile);
        Vec2i blChunk = Island.getBottomLeftBound(dim, tile);
        CompletableFuture<Void>[] clearChunkFs = new CompletableFuture[(trChunk.getX() - blChunk.getZ() + 1) * (trChunk.getZ() - blChunk.getZ() + 1)];
        int i = 0;
        for(int z = blChunk.getZ(); z < trChunk.getZ(); z++) {
            for(int x = blChunk.getX(); x < trChunk.getX(); x++) {
                clearChunkFs[i++] = cg.getChunk(dim.getWorld(), x, z).thenAccept((c) -> {
                    Ether.inst().getNms().clearChunk(c, true);
                });
            }
        }
        return CompletableFuture.allOf(clearChunkFs).thenApply((__) -> tile);
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

    public Collection<Vec2i> getPurgeQueue() {
        return Collections.unmodifiableCollection(grid.getPurgeQueue());
    }
}
