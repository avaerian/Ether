package org.minerift.ether.util.collect;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntUnaryOperator;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static java.lang.String.format;

public class Int2ObjectIdentityMap<V> implements Int2ObjectMap<V> {

    private static final int DEFAULT_INIT_SIZE = 16;
    private static final int DEFAULT_ENTRY_LIMIT = 1024;
    private static final IntUnaryOperator DEFAULT_GROWER = (i) -> i == 0 ? 2 : i * 2;

    private Object[] map;
    private int size;
    private int limit;
    private V defaultRet;
    private IntUnaryOperator grower;

    public Int2ObjectIdentityMap(int limit, IntUnaryOperator grower) {
        this.map = new Object[DEFAULT_INIT_SIZE];
        this.size = 0;
        this.limit = limit;
        this.defaultRet = null;
        this.grower = grower;
    }

    public Int2ObjectIdentityMap(int limit) {
        this(limit, DEFAULT_GROWER);
    }

    public Int2ObjectIdentityMap() {
        this(DEFAULT_ENTRY_LIMIT, DEFAULT_GROWER);
    }

    @Override
    public V put(int key, V value) {
        if(key >= limit) {
            throw new IllegalArgumentException(format("Key (%d) exceeds limit (%d)", key, limit));
        }
        if(key >= map.length) {
            int newLen = grower.apply(map.length);
            if(newLen < key) {
                newLen = key;
            }
            Object[] copy = new Object[newLen];
            System.arraycopy(map, 0, copy, 0, map.length);
            map = copy;
        }
        V old = (V) map[key];
        if(old == null && value == null) {
            return defaultRet;
        }
        map[key] = value;
        if(old == null) {
            size++;
        } else if(value == null) {
            size--;
        }
        return old == null ? defaultRet : old;
    }

    @Override
    public V remove(int key) {
        V old = (V) map[key];
        if(old == null) {
            return defaultRet;
        }
        map[key] = null;
        size--;
        return old;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean containsValue(Object value) {
        for(Object obj : map) {
            if(value.equals(obj)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void putAll(@NotNull Map<? extends Integer, ? extends V> m) {

    }

    @Override
    public void defaultReturnValue(V v) {
        this.defaultRet = v;
    }

    @Override
    public V defaultReturnValue() {
        return defaultRet;
    }

    @Override
    public ObjectSet<Entry<V>> int2ObjectEntrySet() {
        return null;
    }

    @Override
    public IntSet keySet() {
        return null;
    }

    @Override
    public ObjectCollection<V> values() {
        return null;
    }

    @Override
    public V get(int i) {
        return null;
    }

    @Override
    public boolean containsKey(int i) {
        return false;
    }
}
