package org.minerift.ether.util.nbt.transmute;

import org.minerift.ether.debug.Experimental;
import org.minerift.ether.util.nbt.tags.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

// R -> result
// Unwrap different tag types to a common value type
@Experimental
public class UnwrapTags<R> {

    public static <R> Mutable<R> to(Class<R> result) {
        return new Mutable<>(result);
    }

    private final Mutable<R> chain;
    public UnwrapTags(Class<R> result, List<Mutable<R>.Entry> entries) {
        this.chain = new Mutable<R>(result, entries);
    }

    public UnwrapTags(Mutable<R> chain) {
        this.chain = chain;
    }

    public R unwrap(Tag tag) throws NbtTransmuteException {
        return chain.unwrap(tag);
    }

    public <E extends Exception> R unwrap(Tag tag, Function<NbtTransmuteException, E> ex) throws E {
        return chain.unwrap(tag, ex);
    }

    public static class Mutable<R> {
        private final Class<R> result;
        private final List<Entry> entries;
        protected Mutable(Class<R> result) {
            this.result = result;
            this.entries = new ArrayList<>();
        }

        protected Mutable(Class<R> result, List<Entry> entries) {
            this.result = result;
            this.entries = entries;
        }

        // when using, ensure predicate tests if tag is of type T
        public <T extends Tag> Mutable<R> add(Class<T> type, Predicate<Tag> test, Function<T, R> unwrap) {
            Entry entry = new Entry(test, (Function<Tag, R>) unwrap);
            entries.add(entry);
            return this;
        }

        public R unwrap(Tag tag) throws NbtTransmuteException {
            for(Entry entry : entries) {
                if(entry.test.test(tag)) {
                    return entry.unwrap.apply(tag);
                }
            }
            throw new NbtTransmuteException("Failed unwrapping tag " + tag.getTypeName() + " to " + result);
        }

        public <E extends Exception> R unwrap(Tag tag, Function<NbtTransmuteException, E> ex) throws E {
            try {
                return unwrap(tag);
            } catch (NbtTransmuteException e) {
                throw ex.apply(e);
            }
        }

        public UnwrapTags<R> immutable() {
            return new UnwrapTags<>(this);
        }

        public class Entry {
            private Predicate<Tag> test;
            private Function<Tag, R> unwrap;
            public Entry(Predicate<Tag> test, Function<Tag, R> unwrap) {
                this.test = test;
                this.unwrap = unwrap;
            }
        }

    }

}
