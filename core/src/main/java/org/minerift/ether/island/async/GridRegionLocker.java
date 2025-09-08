package org.minerift.ether.island.async;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.minerift.ether.math.Vec2i;

import java.util.concurrent.locks.StampedLock;

// TODO: refactor this class as an interface with different impls
//  i.e. BitSet for contiguous island ids (thinking all loaded),
//       int[] buffer for larger island counts (there shouldn't be
//       too many locked tiles at a given time; only locked to avoid
//       data races for async situations
public class GridRegionLocker {

    private volatile Int2ObjectMap<StampedLock> tile2Lock;
    //private BitSet tiles; // review

    // NOTE: IslandGrid operations will be written synchronously; managing between islands will be async (locking tiles)
    public GridRegionLocker() {
        this.tile2Lock = new Int2ObjectOpenHashMap<>();
        //this.tiles = new BitSet();
    }

    public GridRegionLocker(int expect) {
        this.tile2Lock = new Int2ObjectOpenHashMap<>(expect);
    }

    private GridRegionLocker(Int2ObjectMap<StampedLock> tile2Lock) {
        this.tile2Lock = tile2Lock;
    }

    public void free(Vec2i tile) {
        free(tile.getTileId());
    }

    public void free(int id) {
        if(tile2Lock.remove(id) != null) {
            // lock entry did exist and was removed; log
            // NOTE: I don't believe this frees the existing read/write locks associated,
            // so that needs to be accounted for
        }
    }

    public StampedLock getRawLock(Vec2i tile) {
        return getRawLock(tile.getTileId());
    }

    public StampedLock getRawLock(int id) {
        return tile2Lock.get(id);
    }

    public long readLock(Vec2i tile) {
        return readLock(tile.getTileId());
    }

    public long readLock(int id) {
        StampedLock lock = tile2Lock.computeIfAbsent(id, (_id) -> new StampedLock());
        return lock.readLock();
    }

    public long writeLock(int id) {
        StampedLock lock = tile2Lock.computeIfAbsent(id, (_id) -> new StampedLock());
        return lock.writeLock();
    }

}
