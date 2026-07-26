package org.minerift.ether.island;

import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.minerift.ether.Ether;
import org.minerift.ether.config.ConfigType;
import org.minerift.ether.config.islandspecs.IslandSpec;
import org.minerift.ether.config.main.MainConfig;
import org.minerift.ether.dimension.Dimension;
import org.minerift.ether.math.Vec2i;
import org.minerift.ether.math.Vec3;
import org.minerift.ether.math.Vec3i;
import org.minerift.ether.nms.NMSAccess;
import org.minerift.ether.nms.world.ChunkGetter;
import org.minerift.ether.nms.world.ItemStack;
import org.minerift.ether.panel.Panel;
import org.minerift.ether.panel.PanelIcon;
import org.minerift.ether.schematic.Schematic;
import org.minerift.ether.schematic.SchematicPasteOptions;
import org.minerift.ether.user.EtherUser;
import org.minerift.ether.util.BukkitUtils;
import org.minerift.ether.world.ChunkCoords;
import org.minerift.ether.world.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class IslandManager {

    public static final Logger LOGGER = LoggerFactory.getLogger(IslandManager.class);
    public static final Panel PANEL = Panel.builder(Component.text("Manage your Island"), 6)
            .put(0, PanelIcon.of(ItemStack.of("minecraft:grass_block"), (user) -> {
                Player plr = user.getPlayer().orElseThrow();
                Island island = user.getIsland().orElse(null);
                if(island == null)
                    plr.sendMessage(Component.text("You don't have an island!").color(TextColor.color(255, 0, 0)));
                plr.teleportAsync(BukkitUtils.asBukkitLocation(plr.getWorld(), island.getSpawn()));
            }))
            .build();

    private final IslandGrid grid;

    private Map<Vec2i, ReadWriteLock> lockedTiles;
    private long nextClearTimestamp;

    public IslandManager(IslandGrid grid, int timeUntilNextPurgeSecs) {
        this.grid = grid;
        this.lockedTiles = new ConcurrentHashMap<>();
        this.nextClearTimestamp += System.currentTimeMillis()
                + TimeUnit.SECONDS.toMillis(timeUntilNextPurgeSecs);
        // TODO: create task for checking if islands needs to be cleared
    }

    public IntSet getKeySet() {
        return grid.getIslandIds();
    }

    // NOTE: careful when tinkering with the locks
    public Map<Vec2i, ReadWriteLock> getLockedTiles() {
        return Collections.unmodifiableMap(lockedTiles);
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
    public CompletableFuture<Island> createIsland(Dimension dim, IslandSpec spec, EtherUser owner) {

        owner.getPlayer().orElseThrow(() -> new IllegalArgumentException("User must be online to create island"));
        final World world = dim.getWorld(); // TODO: review this;

        final Vec2i tile = grid.getNextTile();
        final ReadWriteLock lock = lockedTiles.computeIfAbsent(tile, (__) -> new ReentrantReadWriteLock());
        lock.writeLock().lock(); // lock should be unlocked after future is run
        LOGGER.warn("Write lock locked");

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
            DeletedTile deleted = syncGrid.getTileForClearing(tile);
            if(deleted != null && deleted.dimsToClear().contains(dim.getResourceLocation())) {
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
                    .setTile(tile, true);

            final Island island = builder.build();
            syncGrid.registerIsland(island);

            owner.setIsland(island);

            Ether.LOGGER.warn("Created island at {}", island.getTile());

            owner.getPlayer().orElseThrow(() -> new IllegalStateException("User offline before island creation completed"))
                    .teleportAsync(BukkitUtils.asBukkitLocation(world, centerBlock));
            return island;
        }).whenComplete((o, ex) -> {
            lock.writeLock().unlock();
            LOGGER.warn("Write lock unlocked");
        }); // unlock tile
    }

    protected CompletableFuture<DeletedTile[]> clearPurgeQueue() {
        DeletedTile[] tiles = getPurgeQueue().toArray(DeletedTile[]::new);
        return CompletableFuture.supplyAsync(() -> {
            for(DeletedTile tile : getPurgeQueue()) {
                /*Dimension.from()
                for() {

                }*/
            }
            return tiles;
        });
    }

    public CompletableFuture<DeletedTile> deleteIsland(Island island, ChunkGetter cg) {
        final MainConfig cfg = Ether.inst().getConfig(ConfigType.MAIN);
        final CompletableFuture<DeletedTile> cf;
        final ReadWriteLock lock = lockedTiles.computeIfAbsent(island.getTile(), (__) -> new ReentrantReadWriteLock());
        lock.writeLock().lock();
        LOGGER.warn("Write lock locked");
        switch(cfg.purgeIslandsOption()) {
            case THRESHOLD -> { // FIXME: review; too laggy??
                // proposed solution: clear tiles one at a time until each is complete
                // - possible time margin between each clearing
                // - clearing all tiles at once seems to add too much workload at once

                // proposed implementation:
                // - CompletableFuture for each tile
                // - CompletableFuture is immediately locked until

                grid.queueForClearing(island);
                final Collection<DeletedTile> queue = Collections.synchronizedCollection(grid.getPurgeQueue());
                CompletableFuture<Vec2i>[] clearAllTilesFs = new CompletableFuture[queue.size()];
                int i = 0;
                // TODO: refactor this code into separate clearQueue function
                // TODO: refactor to instead delete only this island, then call to clear queue
                if(queue.size() >= cfg.getPurgedIslandsThreshold()) {
                    for(DeletedTile tile : queue) {
                        for(Dimension dim : cfg.getDimensions()) {
                            // TODO: review this
                            CompletableFuture<Vec2i> _cf = clearTile(dim, tile.tile(), cg).thenApply((tile0) -> {
                                queue.remove(tile);
                                return tile0;
                            });
                            clearAllTilesFs[i++] = _cf;
                            _cf.join();
                        }
                    }
                }
                cf = CompletableFuture.allOf(clearAllTilesFs)
                        .thenApply((__) -> DeletedTile.from(island.getTile(), cfg.getDimensions()));
            }

            case QUEUED -> {
                grid.queueForClearing(island);
                cf = CompletableFuture.completedFuture(DeletedTile.from(island.getTile(), cfg.getDimensions()));
            }

            default -> throw new IllegalStateException("Unexpected value: " + cfg.purgeIslandsOption());
        }
        cf.whenComplete((o, ex) -> {
            lock.writeLock().unlock();
            LOGGER.warn("Write lock unlocked"); // debug; FIXME: allow debug level to work
        }); // unlock tile

        grid.unregisterIsland(island);

        // Remove island references from players on island team
        island.getTeamMembers().forEach(island::removeTeamMember);
        // Teleport players back to spawn

        return cf;
    }

    // NOTE: doesn't handle locking
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

    public CompletableFuture<DeletedTile> deleteIsland(int islandId, ChunkGetter cg) {
        return deleteIsland(grid.getIslandAt(islandId).orElseThrow(), cg);
    }

    public CompletableFuture<DeletedTile> deleteIsland(Vec2i tile, ChunkGetter cg) {
        return deleteIsland(grid.getIslandAt(tile).orElseThrow(), cg);
    }

    public CompletableFuture<DeletedTile> deleteIsland(Island island) {
        return deleteIsland(island, ChunkGetter.SYNC);
    }

    public CompletableFuture<DeletedTile> deleteIsland(int islandId) {
        return deleteIsland(grid.getIslandAt(islandId).orElseThrow(), ChunkGetter.SYNC);
    }

    public CompletableFuture<DeletedTile> deleteIsland(Vec2i tile) {
        return deleteIsland(grid.getIslandAt(tile).orElseThrow(), ChunkGetter.SYNC);
    }

    public Optional<Island> getIslandAt(Vec2i tile) {
        return grid.getIslandAt(tile);
    }

    public Optional<Island> getIslandAt(int islandId) {
        return grid.getIslandAt(islandId);
    }

    public Optional<Island> getIslandAt(Location loc) {
        return getIslandAt(ChunkCoords.getChunkAt(loc.getX(), loc.getZ()));
    }

    public Optional<Island> getIslandAt(int blockX, int blockZ) {
        return getIslandAt(ChunkCoords.getChunkAt(blockX, blockZ));
    }

    public Optional<Island> getIslandAt(Vec3 blockPos) {
        return getIslandAt(ChunkCoords.getChunkAt(blockPos));
    }

    public Collection<DeletedTile> getPurgeQueue() {
        return Collections.unmodifiableCollection(grid.getPurgeQueue());
    }
}
