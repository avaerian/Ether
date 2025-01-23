package org.minerift.ether.util;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

// Represents a resizable list with an indexer function
// Primary use is for IslandGrid
public class IndexedList<T> implements Iterable<T> {
    private ArrayList<T> buffer;
    private Function<T, Integer> index;
    private Predicate<T> canReplace;

    public static <T> IndexedList<T> createUnreplaceable(int initialSize, Function<T, Integer> index) {
        return new IndexedList<>(initialSize, index, Predicates.never());
    }

    public static <T> IndexedList<T> createUnreplaceable(Function<T, Integer> index) {
        return new IndexedList<>(index, Predicates.never());
    }

    public IndexedList(int initialSize, Function<T, Integer> index, Predicate<T> canReplace) {
        this.buffer = new ArrayList<>(initialSize);
        this.index = index;
        this.canReplace = canReplace;
    }

    public IndexedList(Function<T, Integer> index, Predicate<T> canReplace) {
        this(10, index, canReplace);
    }

    public void readjust(int size) {
        if(size >= buffer.size()) {
            int diff = size - buffer.size() + 1;
            buffer.addAll(Arrays.asList((T[]) new Object[diff])); // append null elements to end
        }
    }

    // Reduced checks for the add() method for when the size is adjusted and known
    // Returns the old element
    public T add(T t) {
        final int idx = index.apply(t);
        readjust(idx);
        try {
            final T existing = buffer.get(idx);
            if(existing != null && !canReplace(existing)) {
                throw new UnsupportedOperationException("Can't replace index " + idx + "!");
            }
        } catch(IndexOutOfBoundsException ignore) {}

        return buffer.set(idx, t);
    }

    public T get(int idx) {
        return buffer.get(idx);
    }

    public T set(int idx, T val) {
        return buffer.set(idx, val);
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

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return buffer.iterator();
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        buffer.forEach(action);
    }

    @Override
    public Spliterator<T> spliterator() {
        return buffer.spliterator();
    }

    public static class Predicates {
        private static final Predicate<?> ALWAYS = (ignore) -> true;
        private static final Predicate<?> NEVER = (ignore) -> false;

        public static <E> Predicate<E> always() {
            return (Predicate<E>) ALWAYS;
        }

        public static <E> Predicate<E> never() {
            return (Predicate<E>) NEVER;
        }
    }
}
