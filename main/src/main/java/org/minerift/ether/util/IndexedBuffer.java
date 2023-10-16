package org.minerift.ether.util;

import com.google.common.collect.ImmutableList;
import org.minerift.ether.island.Island;

import java.util.ArrayList;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

// Represents a resizable buffer that can be indexed
// Indexes begin at 0 and cannot be negative
// Primary use is for IslandGrid
public class IndexedBuffer<T> {

    private ArrayList<T> buffer;
    private Function<T, Integer> index;
    private Predicate<T> canReplace;

    public static <T> IndexedBuffer<T> createUnreplaceable(int initialSize, Function<T, Integer> index) {
        return new IndexedBuffer<>(initialSize, index, Predicates.never());
    }

    public static <T> IndexedBuffer<T> createUnreplaceable(Function<T, Integer> index) {
        return new IndexedBuffer<>(index, Predicates.never());
    }

    public IndexedBuffer(int initialSize, Function<T, Integer> index, Predicate<T> canReplace) {
        this.buffer = new ArrayList<>(initialSize);
        this.index = index;
        this.canReplace = canReplace;
    }

    public IndexedBuffer(Function<T, Integer> index, Predicate<T> canReplace) {
        this(10, index, canReplace);
    }

    public void readjust(int size) {
        buffer.ensureCapacity(size);
    }

    // Reduced checks for the add() method for when the size is adjusted and known
    // Returns the old element
    public T add(T t) {
        final int idx = index.apply(t);
        readjust(idx);
        final T existing = buffer.get(idx);
        if(existing != null && !canReplace(existing)) {
            throw new UnsupportedOperationException("Can't replace index " + idx + "!");
        }
        return buffer.set(idx, t);
    }

    public T get(int idx) {
        return buffer.get(idx);
    }

    public T remove(T t) {
        return remove(index.apply(t));
    }

    public T remove(int idx) {
        return buffer.remove(idx);
    }

    public int size() {
        return buffer.size();
    }

    public Stream<T> stream() {
        return buffer.stream();
    }

    public ImmutableList<T> getImmutableView() {
        return ImmutableList.copyOf(buffer);
    }

    private boolean canReplace(T existing) {
        return canReplace != null && canReplace.test(existing);
    }

    public static class Predicates {
        private static final Predicate<?> ALWAYS = (ignore) -> true;
        private static final Predicate<?> NEVER = (ignore) -> false;

        public static final <E> Predicate<E> always() {
            return (Predicate<E>) ALWAYS;
        }

        public static final <E> Predicate<E> never() {
            return (Predicate<E>) NEVER;
        }
    }

    public static final IndexedBuffer<Island> ISLAND_GRID = new IndexedBuffer<Island>(Island::getId, Island::isDeleted);
}
