package org.minerift.ether.island;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.minerift.ether.math.GridAlgorithm;
import org.minerift.ether.math.Vec2i;
import org.minerift.ether.util.collect.Int2ObjectIdentityMap;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DefaultIslandGrid implements IslandGrid {

    private final Int2ObjectIdentityMap<Island> islands;
    public final Set<Vec2i> needsClearing;
    private final ReadWriteLock lock;

    public DefaultIslandGrid(Int2ObjectIdentityMap<Island> islands, Set<Vec2i> needsClearing) {
        this.islands = islands;
        this.needsClearing = needsClearing;
        this.lock = new ReentrantReadWriteLock();
    }

    public DefaultIslandGrid(Int2ObjectIdentityMap<Island> islands) {
        this(islands, new HashSet<>());
    }

    public DefaultIslandGrid() {
        this(new Int2ObjectIdentityMap<>(), new HashSet<>());
    }

    @Override
    public void registerIsland(Island island) {
        if(islands.putIfAbsent(island.getId(), island) != null)
            throw new IllegalStateException("Island already exists at " + island.getTile());
    }

    @Override
    public void unregisterIsland(int id) {
        islands.remove(id);
    }

    @Override
    public Optional<Island> getIslandAt(int id) {
        return Optional.ofNullable(islands.get(id));
    }

    @Override
    public boolean isTileOccupied(int id) {
        return islands.containsKey(id);
    }

    @Override
    public boolean needsClearing(Vec2i tile) {
        return needsClearing.contains(tile);
    }

    @Override
    public int getIslandCount() {
        return islands.size();
    }

    @Override
    public ImmutableList<Island> getIslandsView() {
        return ImmutableList.copyOf(islands.stream().map(Int2ObjectIdentityMap.Entry::getValue).iterator());
    }

    @Override
    public ImmutableList<Vec2i> getAvailableTiles() {
        ImmutableList.Builder<Vec2i> list = ImmutableList.builder();
        final int gridEnd = getNextIdGridEnd();
        for(int i = -1; i != Integer.MAX_VALUE && (i = islands.nextAvailableKey(i+1)) != -1 && i != gridEnd;)
            list.add(GridAlgorithm.computeTile(i));
        return list.build();
    }

    private int getNextIdGridEnd() {
        int i;
        for(i = -1; i != Integer.MAX_VALUE && (i = islands.nextUsedKey(i+1)) != -1;);
        return ++i;
    }

    @Override
    public Vec2i getNextTile() {
        return GridAlgorithm.computeTile(islands.nextAvailableKey());
    }

    @Override
    public IntSet getIslandIds() {
        return islands.keySet();
    }

    @Override
    public Lock readLock() {
        return lock.readLock();
    }

    @Override
    public Lock writeLock() {
        return lock.writeLock();
    }
}
