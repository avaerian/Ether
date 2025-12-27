package org.minerift.ether.util.collect;

import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.jetbrains.annotations.NotNull;
import org.minerift.ether.debug.Debug;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;

public class IntBitmapSet implements IntSet, IntCollection {

    private BitSet set;

    public IntBitmapSet() {
        this.set = new BitSet();
    }

    public IntBitmapSet(int bits) {
        this.set = new BitSet(bits);
    }

    public IntBitmapSet(BitSet set) {
        this.set = set;
    }

    public IntBitmapSet(IntCollection c) throws IndexOutOfBoundsException {
        this.set = new BitSet();
        for(IntIterator it = c.intIterator(); it.hasNext();) {
            set.set(it.nextInt());
        }
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        if(c instanceof IntCollection ic) {
            return containsAll(ic);
        }

        Collection<Integer> ic = (Collection<Integer>) c;
        for(int i : ic) {
            if(!contains(i)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends Integer> c) {
        if(c instanceof IntCollection ic) {
            return addAll(ic);
        }

        int hash = hashCode();
        for(int i : c) {
            set.set(i);
        }
        return hash != hashCode();
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        if(c instanceof IntCollection ic) {
            return addAll(ic);
        }

        Collection<Integer> ic = (Collection<Integer>) c;
        int hash = hashCode();
        for(int i : ic) {
            set.clear(i);
        }
        return hash != hashCode();
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        if(c instanceof IntCollection ic) {
            return addAll(ic);
        }

        Collection<Integer> ic = (Collection<Integer>) c;
        int hash = hashCode();
        BitSet cb = new BitSet();
        for(int i : ic) {
            cb.set(i);
        }
        set.and(cb);
        return hash != hashCode();
    }

    @Override
    public int size() {
        return set.cardinality();
    }

    @Override
    public boolean isEmpty() {
        return set.cardinality() == 0;
    }

    @Override
    public boolean contains(int key) {
        return set.get(key);
    }

    // true if not already present
    @Override
    public boolean add(int key) {
        boolean has = set.get(key);
        set.set(key);
        return !has;
    }

    // true if this set contained the specified element
    @Override
    public boolean remove(int k) {
        boolean has = set.get(k);
        set.clear(k);
        return has;
    }

    @Override
    public void clear() {
        set.clear();
    }

    @Override
    public @NotNull IntIterator iterator() {
        return new IntIterator() {
            int i = -1;

            @Override
            public int nextInt() {
                return i;
            }

            @Override
            public boolean hasNext() {
                return i != Integer.MAX_VALUE && (i = set.nextSetBit(i+1)) != -1;
            }
        };
        //return IntIterators.asIntIterator(set.stream().iterator());
    }

    @Override
    public boolean addAll(IntCollection c) {
        int hash = set.hashCode();
        for(IntIterator it = c.intIterator(); it.hasNext();) {
            set.set(it.nextInt());
        }
        return hash != set.hashCode();
    }

    @Override
    public boolean containsAll(IntCollection c) {
        long[] longs = set.toLongArray(); // copy internal array
        for(IntIterator it = c.intIterator(); it.hasNext();) {
            int i = it.nextInt();
            longs[i >> 6] |= (1L << (i & 63));
        }
        return set.equals(BitSet.valueOf(longs));
    }

    @Debug
    public static void main(String[] args) {
        for(int i = 0; i < 278; i++) {
            System.out.println((i & 63) + " -> " + (i >> 6));
        }

        BitSet set = new BitSet();
        set.set(5, 21);
        int[] ret = new int[set.cardinality()];
        for(int i = -1, j = 0; i != Integer.MAX_VALUE && (i = set.nextSetBit(i+1)) != -1; j++) {
            ret[j] = i;
        }
        System.out.println(Arrays.toString(ret));
    }

    @Override
    public boolean removeAll(IntCollection c) {
        int hash = hashCode();
        for(IntIterator it = c.intIterator(); it.hasNext();) {
            set.clear(it.nextInt());
        }
        return hash != hashCode();
    }

    @Override
    public boolean retainAll(IntCollection c) {
        int hash = hashCode();
        BitSet cb = new BitSet();
        for(IntIterator it = c.intIterator(); it.hasNext();) {
            cb.set(it.nextInt());
        }
        set.and(cb);
        return hash != hashCode();
    }

    @Override
    public int[] toIntArray() {
        int[] ret = new int[set.cardinality()];
        for(int i = -1, j = 0; i != Integer.MAX_VALUE && (i = set.nextSetBit(i+1)) != -1; j++) {
            ret[j] = i;
        }
        return ret;
    }

    @Override
    public int[] toArray(int[] a) {
        int size = set.cardinality();
        if(a.length > size) {
            a = new int[size];
        }
        for(int i = 0, j = -1; i < size && i < a.length; i++) {
            a[i] = (j = set.nextSetBit(j+1));
        }
        return a;
    }

    @Override
    public @NotNull Integer[] toArray() {
        Integer[] ret = new Integer[set.cardinality()];
        for(int i = -1, j = 0; i != Integer.MAX_VALUE && (i = set.nextSetBit(i+1)) != -1; j++) {
            ret[j] = i;
        }
        return ret;
    }

    @Override
    public @NotNull <T> T[] toArray(@NotNull T[] a) throws ArrayStoreException {
        final int size = set.cardinality();
        if(a.length < size) a = (T[]) new Object[size];

        for(int i = -1, j = 0; i != Integer.MAX_VALUE && (i = set.nextSetBit(i+1)) != -1; j++) {
            a[j] = (T) Integer.valueOf(i);
        }
        if(a.length > size) a[size] = null;
        return a;
    }

    @Override
    public int hashCode() {
        return set.hashCode();
    }
}
