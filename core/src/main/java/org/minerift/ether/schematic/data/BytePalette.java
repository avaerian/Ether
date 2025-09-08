package org.minerift.ether.schematic.data;

import it.unimi.dsi.fastutil.objects.Object2ByteOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.minerift.ether.debug.Debug;
import org.minerift.ether.debug.Experimental;
import org.minerift.ether.debug.NeedsTesting;
import org.minerift.ether.math.Maths;
import org.minerift.ether.util.fn.Copy;
import org.minerift.ether.util.iter.ByteIterator;

import java.util.Arrays;
import java.util.Iterator;
import java.util.function.IntUnaryOperator;
import java.util.function.UnaryOperator;

import static java.lang.String.format;

public interface BytePalette<V> extends Iterable<BytePalette.Entry<V>>, Copy<BytePalette<V>> /* implements Map<Byte, V> */ {

    IntUnaryOperator DEFAULT_GROWER = (i) -> i == 0 ? 2 : i * 2;
    int INIT_CAPACITY = 16;

    static <V> Synchronized<V> synchronize() {
        return new Synchronized<>(of());
    }

    static <V> Synchronized<V> synchronize(int expectedSize) {
        return new Synchronized<>(of(expectedSize));
    }

    static <V> Synchronized<V> synchronize(int expectedSize, IntUnaryOperator grower) {
        return new Synchronized<>(of(expectedSize, grower));
    }

    static <V> Synchronized<V> synchronize(BytePalette<V> palette) {
        return palette instanceof Synchronized<V> s ? s : new Synchronized<>(palette);
    }

    static <V> BytePalette<V> of() {
        return new Impl<>(INIT_CAPACITY, DEFAULT_GROWER);
    }

    static <V> BytePalette<V> of(int expectedSize) {
        return new Impl<>(expectedSize, DEFAULT_GROWER);
    }

    static <V> BytePalette<V> of(int expectedSize, IntUnaryOperator grower) {
        return new Impl<>(expectedSize, grower);
    }

    V add(byte key, V val, boolean replace);
    default V add(byte key, V val) {
        return add(key, val, false);
    }

    // Removes the element associated with the key
    // Return old element, or null if no element with key
    V remove(byte key);

    V get(byte key);
    V getOrThrow(byte key) throws IllegalArgumentException;

    byte getKey(V val);
    byte getKeyOrThrow(V val);

    byte nextId();
    int size();

    boolean containsKey(byte key);
    boolean containsValue(V val);

    default BytePalette.Synchronized<V> sync() {
        return this instanceof Synchronized<V> s ? s : new Synchronized<>(this);
    }

    // For single-threaded, read-only purposes
    Iterator<Entry<V>> fastIterator();

    ByteIterator keyIterator();
    Iterator<V> valueIterator();

    BytePalette<V> copy(UnaryOperator<V> copier);

    class Impl<V> implements BytePalette<V> {
        //private static final byte[] EMPTY_KEYS = new byte[0];
        //private static final Object[] EMPTY_VALUES = new Object[0];

        protected final IntUnaryOperator grower;
        protected byte[] keys;
        protected Object[] vals;
        protected Object2ByteOpenHashMap<V> revLookup;
        protected int size;

        private Impl(byte[] keys, Object[] vals, int size, IntUnaryOperator grower, Object2ByteOpenHashMap<V> revLookup) {
            this.keys = keys;
            this.vals = vals;
            this.revLookup = revLookup;
            this.grower = grower;
            this.size = size;
        }

        protected Impl(int expectedSize, IntUnaryOperator grower) {
            this.keys = new byte[expectedSize];
            this.vals = new Object[expectedSize];
            this.revLookup = new Object2ByteOpenHashMap<>(expectedSize);
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
            BytePalette.Impl<String> test = (Impl) (BytePalette.of());
            /*test.keys = new byte[]{5, 12, 17, 20, 21, 52, 69, 96};

            test.size = test.keys.length;
            int idx = test.findKey((byte)95);
            idx = idx < 0 ? ~idx : idx;
            System.out.println("key idx: " + idx);
            System.out.println(test.keys[idx]);



            BytePalette<Integer> palette = BytePalette.of(6);
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
            System.out.println(palette);*/
        }

        // Return index of desired key, or index for insertion
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

        @Override
        public V add(byte key, V val, boolean replace) { // returns old value
            if(size == 128 && !replace) {
                throw new IllegalArgumentException("BytePalette is full and key " + key + " cannot be replaced");
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
            revLookup.put(val, key);
            return existing;
        }

        @Override
        public V get(byte key) {
            int i = findKey(key);
            if(i < 0) {
                return null;
            }

            V val = (V) vals[i];
            return val;
        }

        @Override
        public byte getKey(V val) {
            return revLookup.getByte(val);
        }

        @Override
        public byte getKeyOrThrow(V val) {
            if(!revLookup.containsKey(val)) {
                throw new IllegalArgumentException("Value " + val + " doesn't exist in byte palette");
            }
            return revLookup.getByte(val);
        }

        @Override
        public V getOrThrow(byte key) throws IllegalArgumentException {
            V obj = get(key);
            if(obj == null) {
                throw new IllegalArgumentException("Key " + key + " doesn't exist in byte palette");
            }
            return obj;
        }

        @Override
        public int size() {
            return size;
        }

        @Override
        public boolean containsKey(byte key) {
            return findKey(key) >= 0;
        }

        @Override
        public boolean containsValue(V val) {
            return revLookup.containsKey(val);
        }

        @Override
        public byte nextId() {
            byte b, i = 0;
            for(ByteIterator it = keyIterator(); it.hasNext(); i++) {
                b = it.nextByte();
                if(b != i) {
                    break;
                }
            }
            return i;
        }

        // Removes the element associated with the key
        // Return old element, or null if no element with key
        @NeedsTesting
        @Override
        public V remove(byte key) {
            int i = findKey(key);
            if(i < 0) {
                return null;
            }

            // FIXME: rewrite this; WTF??
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
            revLookup.removeByte(old);
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
        @Override
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

        @Override
        public ByteIterator keyIterator() {
            return ByteIterator.of(keys, size, 0);
        }

        @Override
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
            return new BytePalette.Impl<>(ckeys, cvals, size, grower, revLookup);
        }

        @Override
        public BytePalette<V> copy(UnaryOperator<V> copier) { // update to (byte, V) -> V lambda ?
            byte[] ckeys = new byte[keys.length];
            Object[] cvals = new Object[vals.length];
            System.arraycopy(keys, 0, ckeys, 0, keys.length);
            for(int i = 0; i < vals.length; i++) {
                cvals[i] = copier.apply((V) cvals[i]);
            }
            return new BytePalette.Impl<>(ckeys, cvals, size, grower, revLookup);
        }
    }

    // review this class
    @Experimental
    @NeedsTesting
    final class Synchronized<V> implements BytePalette<V> {

        private BytePalette<V> plt;

        protected Synchronized(BytePalette<V> plt) {
            this.plt = plt;
        }

        @Override
        public synchronized V add(byte key, V val, boolean replace) {
            return plt.add(key, val, replace);
        }

        @Override
        public synchronized V add(byte key, V val) {
            return plt.add(key, val);
        }

        @Override
        public synchronized V remove(byte key) {
            return plt.remove(key);
        }

        @Override
        public synchronized V get(byte key) {
            return plt.get(key);
        }

        @Override
        public synchronized V getOrThrow(byte key) throws IllegalArgumentException {
            return plt.getOrThrow(key);
        }

        @Override
        public synchronized byte getKey(V val) {
            return plt.getKey(val);
        }

        @Override
        public synchronized byte getKeyOrThrow(V val) {
            return plt.getKeyOrThrow(val);
        }

        @Deprecated
        @Override
        public Synchronized<V> sync() {
            return this;
        }

        public BytePalette<V> unsync() {
            return plt;
        }

        @Override
        public synchronized byte nextId() {
            return plt.nextId();
        }

        @Override
        public synchronized int size() {
            return plt.size();
        }

        @Override
        public synchronized boolean containsKey(byte key) {
            return plt.containsKey(key);
        }

        @Override
        public synchronized boolean containsValue(V val) {
            return plt.containsValue(val);
        }

        // TODO: review iterators
        @Override
        public synchronized @NotNull Iterator<Entry<V>> iterator() {
            return null;
        }

        @Override
        public synchronized Iterator<Entry<V>> fastIterator() {
            return null;
        }

        @Override
        public synchronized ByteIterator keyIterator() {
            return plt.keyIterator();
        }

        @Override
        public Iterator<V> valueIterator() {
            return null;
        }

        // TODO: review copying
        @Override
        public BytePalette<V> copy(UnaryOperator<V> copier) {
            return null;
        }

        @Override
        public BytePalette<V> copy() {
            return null;
        }

        @Override
        public String toString() {
            return plt.toString();
        }

        @Override
        public int hashCode() {
            return plt.hashCode();
        }
    }

    class Entry<V> {
        protected byte key;
        protected V val;

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
