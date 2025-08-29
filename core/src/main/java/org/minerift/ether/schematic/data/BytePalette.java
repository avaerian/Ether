package org.minerift.ether.schematic.data;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.minerift.ether.debug.Debug;
import org.minerift.ether.debug.NeedsTesting;
import org.minerift.ether.math.Maths;
import org.minerift.ether.util.fn.Copy;
import org.minerift.ether.util.iterator.ByteIterator;

import java.util.Arrays;
import java.util.Iterator;
import java.util.function.IntUnaryOperator;
import java.util.function.UnaryOperator;

import static java.lang.String.format;

public class BytePalette<V> implements Iterable<BytePalette.Entry<V>>, Copy<BytePalette<V>> /* TODO: implements Map<Byte, V> */ {

    //private static final byte[] EMPTY_KEYS = new byte[0];
    //private static final Object[] EMPTY_VALUES = new Object[0];

    public static final IntUnaryOperator DEFAULT_GROWER = (i) -> i == 0 ? 2 : i * 2;
    public static final int INIT_CAPACITY = 16;

    private final IntUnaryOperator grower;
    private byte[] keys;
    private Object[] vals;

    private int size;

    private BytePalette(byte[] keys, Object[] vals, int size, IntUnaryOperator grower) {
        this.grower = grower;
        this.keys = keys;
        this.vals = vals;
        this.size = size;
    }
    
    // TODO: desire only positive byte keys (range 0-127)
    public BytePalette() {
        this(INIT_CAPACITY, DEFAULT_GROWER);
    }

    public BytePalette(int expectedSize) {
        this(expectedSize, DEFAULT_GROWER);
    }

    public BytePalette(int expectedSize, IntUnaryOperator grower) { // TODO: consider unsigned byte size (255) ??
        Preconditions.checkArgument(expectedSize > 0);
        Preconditions.checkArgument(expectedSize <= Byte.MAX_VALUE);
        this.keys = new byte[expectedSize];
        this.vals = new Object[expectedSize];
        //this.minN = this.maxN = 0;
        this.grower = grower;
        this.size = 0;
    }

    private void ensureCapacity(int idx) {
        if(idx >= keys.length) {
            int cap = Maths.clamp(grower.applyAsInt(idx), 0, 128);
            byte[] copyKeys = new byte[cap];
            Object[] copyVals = new Object[cap];
            System.arraycopy(keys, 0, copyKeys, 0, keys.length);
            System.arraycopy(vals, 0, copyVals, 0, vals.length);
            this.keys = copyKeys;
            this.vals = copyVals;
        }
    }

    @Debug
    public static void main(String[] args) {
        BytePalette<String> test = new BytePalette<>();
        test.keys = new byte[]{5, 12, 17, 20, 21, 52, 69, 96};

        test.size = test.keys.length;
        int idx = test.findKey((byte)95);
        idx = idx < 0 ? ~idx : idx;
        System.out.println("key idx: " + idx);
        System.out.println(test.keys[idx]);



        BytePalette<Integer> palette = new BytePalette<>(6);
        byte[] junk = new byte[]{5, 12, 17, 20, 21, 52, 69, 96};
        for(int i = 0; i < junk.length; i++) {
            palette.add(junk[i], i);
        }
        System.out.println(palette);
        palette.remove((byte) 16);
        System.out.println(palette);
        palette.remove((byte) 17);
        System.out.println(palette);
        palette.remove((byte) 96);
        System.out.println(palette);
    }

    // Return index of desired key, or index for insertion
    // Binary search
    private int findKey(byte key) {
        int low = 0;
        int high = size - 1;
        while(low <= high) {
            int mid = (low + high) >>> 1;
            if(keys[mid] > key) {
                high = mid - 1;
            } else if(keys[mid] < key) {
                low = mid + 1;
            } else {
                return mid;
            }
        }

        return ~low;
    }

    public V add(byte key, V val) {
        return add(key, val, false);
    }

    public V add(byte key, V val, boolean replace) { // returns old value
        if(size == 128 && !replace) {
            throw new IllegalArgumentException("BytePalette is full and key cannot be replaced");
        }

        int i = findKey(key);
        V existing = null;
        if(i < 0) {
            i = ~i;
            ensureCapacity(i);
            if(size != i) {
                System.arraycopy(keys, i, keys, i + 1, size - i);
                System.arraycopy(vals, i, vals, i + 1, size - i);
            }
            size++;
        } else {
            existing = (V) vals[i];
            if(existing != null) {
                if(!replace) {
                    throw new IllegalArgumentException(
                            format("%b (%s) is already registered as %s and cannot be replaced", key, val, existing));
                }
                // TODO: logger
            }
        }

        keys[i] = key;
        vals[i] = val;
        return existing;
    }

    // TODO: review
    public V get(byte key) {
        Preconditions.checkArgument(key < vals.length);
        int i = findKey(key);
        if(i < 0) {
            throw new IllegalStateException(key + " doesn't exist in byte palette");
        }

        V val = (V) vals[key];
        if(val == null) {

        }
        return val;
    }

    public boolean containsKey(byte key) {
        return findKey(key) >= 0;
    }

    // linear search
    public boolean containsValue(V val) {
        for(Object obj : vals) {
            if(val.equals(obj)) {
                return true;
            }
        }
        return false;
    }

    // Removes the element associated with the key
    // Return old element, or null if no element with key
    @NeedsTesting
    public V remove(byte key) {
        int i = findKey(key);
        if(i < 0) {
            return null;
        }

        final int end = size - i;
        final V old = (V) vals[size];
        if(end > 1) {
            System.arraycopy(keys, i + 1, keys, i, end);
            System.arraycopy(vals, i + 1, vals, i, end);
        } else {
            keys[i] = -1;
            vals[i] = null;
        }
        size--;
        return old;
    }

    @Override
    public @NotNull Iterator<BytePalette.Entry<V>> iterator() {
        return new Iterator<>() {
            private int curr = 0;

            @Override
            public boolean hasNext() {
                return curr < size;
            }

            @Override
            public Entry<V> next() {
                return new Entry<>(keys[curr], (V) vals[curr++]);
            }
        };
    }

    // For single-threaded, read-only purposes
    public Iterator<BytePalette.Entry<V>> fastIterator() {
        return new Iterator<>() {
            private int curr = 0;
            private final Entry<V> entry = new Entry<>((byte) -1, null);

            @Override
            public boolean hasNext() {
                return curr < size;
            }

            @Override
            public Entry<V> next() {
                entry.key = keys[curr];
                entry.val = (V) vals[curr++];
                return entry;
            }
        };
    }

    public ByteIterator keyIterator() {
        return ByteIterator.of(keys);
    }

    public Iterator<V> valueIterator() {
        return new Iterator<>() {
            private int curr = 0;

            @Override
            public boolean hasNext() {
                return curr < size;
            }

            @Override
            public V next() {
                return (V) vals[curr++];
            }
        };
    }

    @Override
    public String toString() {
        return "BytePalette{" +
                "grower=" + grower +
                ", keys=" + Arrays.toString(keys) +
                ", values=" + Arrays.toString(vals) +
                ", size=" + size +
                '}';
    }

    @Override
    public BytePalette<V> copy() {
        byte[] ckeys = new byte[keys.length];
        Object[] cvals = new Object[vals.length];
        System.arraycopy(keys, 0, ckeys, 0, keys.length);
        System.arraycopy(vals, 0, cvals, 0, vals.length);
        return new BytePalette<>(ckeys, cvals, size, grower);
    }

    public BytePalette<V> copy(UnaryOperator<V> copier) { // update to (byte, V) -> V lambda ?
        byte[] ckeys = new byte[keys.length];
        Object[] cvals = new Object[vals.length];
        System.arraycopy(keys, 0, ckeys, 0, keys.length);
        for(int i = 0; i < vals.length; i++) {
            cvals[i] = copier.apply((V) cvals[i]);
        }
        return new BytePalette<>(ckeys, cvals, size, grower);
    }

    public static class Entry<V> {
        private byte key;
        private V val;

        private Entry(byte key, V val) {
            this.key = key;
            this.val = val;
        }

        public byte getKey() {
            return key;
        }

        public V getValue() {
            return val;
        }
    }
}
