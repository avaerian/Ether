package org.minerift.ether.util.collect;

import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.objects.*;
import org.jetbrains.annotations.NotNull;
import org.minerift.ether.debug.NeedsTesting;
import org.minerift.ether.util.Utils;

import java.util.BitSet;
import java.util.Map;

import static java.lang.String.format;

/**
 * An implementation for {@link Int2ObjectMap} backed by an array, using identity mappings of
 * keys as indices, to values; this does, however, restrain the allowed keys from 0 to {@code limit}.
 *
 * <p>
 * {@code limit} exists to avert situations in which excessively high keys are inserted to resize the
 * backing array erroneously or maliciously to increase memory consumption.
 *
 * <p>
 * This implementation allows for O(1) entry and retrieval.
 *
 * <p>
 * <b>NOTE:</b> if desired, this implementation can support null values; this will be set as a TODO
 *
 * @param <V> value type
 */
public class Int2ObjectIdentityMap<V> implements Int2ObjectMap<V> {

    private static final int DEFAULT_INIT_SIZE = 16;
    private static final int DEFAULT_ENTRY_LIMIT = 1024;
    private static final IntUnaryOperator DEFAULT_GROWER = (i) -> i == 0 ? 2 : i * 2;

    public static <V> Int2ObjectIdentityMap<V> noLimit() {
        return new Int2ObjectIdentityMap<>(Integer.MAX_VALUE);
    }

    public static <V> Int2ObjectIdentityMap<V> noLimit(IntUnaryOperator grower) {
        return new Int2ObjectIdentityMap<>(Integer.MAX_VALUE, grower);
    }

    private BitSet keys;
    private Object[] map;
    private int size; // number of elements in map
    private int limit; // exists so large keys don't create massive arrays without explicitly defining it
    private V defaultRet;
    private IntUnaryOperator grower;

    public Int2ObjectIdentityMap(final int limit, IntUnaryOperator grower) {
        this.keys = new BitSet();
        this.map = new Object[DEFAULT_INIT_SIZE];
        this.size = 0;
        this.limit = limit;
        this.defaultRet = null;
        this.grower = grower;
    }

    public Int2ObjectIdentityMap(final int limit) {
        this(limit, DEFAULT_GROWER);
    }

    public Int2ObjectIdentityMap() {
        this(DEFAULT_ENTRY_LIMIT, DEFAULT_GROWER);
    }

    @Override
    public V put(final int key, final V value) {
        if(key < 0)
            throw new IllegalArgumentException(format("Key (%d) below 0 disallowed", key) );
        if(key >= limit)
            throw new IllegalArgumentException(format("Key (%d) exceeds limit (%d)", key, limit));
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
            keys.set(key);
        } else if(value == null) {
            size--;
            keys.clear(key);
        }
        return old == null ? defaultRet : old;
    }

    @Override
    public V remove(final int key) {
        V old = (V) map[key];
        if(old == null) {
            return defaultRet;
        }
        map[key] = null;
        size--;
        keys.clear(key);
        return old;
    }

    @Override
    public boolean containsValue(Object value) {
        for(Object obj : map)
            if(value.equals(obj))
                return true;
        return false;
    }

    @Override
    public V get(final int i) {
        if(i < 0) throw new IllegalArgumentException(format("Key (%d) below 0 disallowed", i));
        if(i >= map.length) return defaultRet;
        V item = (V) map[i];
        return item != null ? item : defaultRet;
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
    public boolean containsKey(final int i) {
        if(i < 0) throw new IllegalArgumentException(format("Key (%d) below 0 disallowed", i));
        return keys.get(i);
        /*final boolean inRange = i >= 0 && i < map.length;
        return inRange && map[i] != null;*/
    }

    // TODO: remember to add key to keys bitset
    @NeedsTesting
    @Override
    public void putAll(@NotNull Map<? extends Integer, ? extends V> m) {
        if(m.isEmpty()) {
            return;
        }

        Map.Entry<? extends Integer, ? extends V>[] entries = m.entrySet().toArray(Map.Entry[]::new);

        // Enter items from old map to this; if keys are
        // bigger than map capacity, queue them for later
        // instead of resizing the map eagerly.
        IntList queued = new IntArrayList(); // sorted entries
        IntList queuedKeys = new IntArrayList(); // sorted keys, for entry indices
        for(int i = 0; i < entries.length; i++) {
            final Map.Entry<? extends Integer, ? extends V> entry = entries[i];
            final int key = entry.getKey();
            final V value = entry.getValue();

            if(key < 0)
                throw new IllegalArgumentException(format("Key (%d) below 0 disallowed", key) );
            if(key >= limit)
                throw new IllegalArgumentException(format("Key (%d) exceeds limit (%d)", key, limit));

            if(key >= map.length) {
                int idx = ~Utils.binarySearch(queuedKeys, key);
                // Every key is unique; the binary search tells us where to insert.
                queuedKeys.add(idx, key);
                queued.add(idx, i);
                continue;
            }

            V old = (V) map[key];
            map[key] = value;
            if(old == null && value != null) {
                size++;
            } else if(old != null && value == null) {
                size--;
            }
        }

        if(!queued.isEmpty()) {
            final int newSize = queuedKeys.getInt(queuedKeys.size() - 1); // consider some extra buffer space
            Object[] resize = new Object[newSize];
            System.arraycopy(map, 0, resize, 0, map.length);
            for(int idx : queued) {
                Map.Entry<? extends Integer, ? extends V> entry = entries[idx];
                resize[entry.getKey()] = entry.getValue();
                if(entry.getValue() != null) {
                    size++;
                }
            }
            map = resize;
        }
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
    public @NotNull IntBitmapSet keySet() {
        return new IntBitmapSet(keys);
    }

    @Override
    public @NotNull ObjectCollection<V> values() {
        return new Values();
    }

    final class Values extends AbstractObjectCollection<V> {
        @Override
        public @NotNull ObjectIterator<V> iterator() {
            return new ObjectIterator<>() {
                int i = -1;

                @Override
                public boolean hasNext() {
                    return i != Integer.MAX_VALUE && (i = keys.nextSetBit(i+1)) != -1;
                }

                @Override
                public V next() {
                    return (V) map[i];
                }
            };
        }

        @Override
        public int size() {
            return size;
        }
    }
}
