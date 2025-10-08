package org.minerift.ether.util.nbt.transmute;

import org.minerift.ether.debug.NeedsTesting;
import org.minerift.ether.util.Either;
import org.minerift.ether.util.nbt.tags.Tag;
import org.minerift.ether.util.nbt.tags.container.CompoundTag;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.minerift.ether.util.nbt.tags.TagTypes.COMPOUND;

// Map tags to different tag types; useful for translating extended types to primitive types
@NeedsTesting
public class MapTags {

    public static final BiFunction<Mutable, CompoundTag, Tag> DEFAULT_COMPOUND_MAPPER;

    static {
        DEFAULT_COMPOUND_MAPPER = (chain, ct) -> {
            CompoundTag copy = ct.copy();
            for(Map.Entry<String, Tag> entry : ct) {
                Either<Function<Tag, Tag>,
                        BiFunction<Mutable, Tag, Tag>> mapping = chain.getMapping(entry.getValue());
                if(mapping != null) {
                    Tag ntag;
                    if(mapping.isLeft()) {
                        ntag = mapping.getLeft().apply(entry.getValue());
                    } else {
                        ntag = mapping.getRight().apply(chain, entry.getValue());
                    }
                    ntag.setName(entry.getKey());
                    copy.addTag(ntag, true);
                }
            }
            return copy;
        };
    }

    public static Mutable of() {
        return new Mutable();
    }

    public static Mutable of(MapTags mappings) {
        return new Mutable(mappings.chain.entries);
    }

    public static Mutable of(Mutable mappingsMut) {
        return new Mutable(mappingsMut.entries);
    }

    private final Mutable chain;
    public MapTags(Mutable chain) {
        this.chain = chain;
    }

    public Tag map(Tag tag) {
        return chain.map(tag);
    }

    public static class Mutable {
        private final List<Entry> entries;
        public Mutable(List<Entry> entries) {
            this.entries = entries;
        }

        public Mutable() {
            this.entries = new ArrayList<>();
            add(CompoundTag.class, (t) -> t.is(COMPOUND), DEFAULT_COMPOUND_MAPPER);
        }

        public <T extends Tag> Mutable add(Class<T> type, Predicate<Tag> test, Function<T, Tag> mapper) {
            entries.add( new Entry(test, Either.left((Function<Tag, Tag>) mapper)) );
            return this;
        }

        public <T extends Tag> Mutable add(Class<T> type, Predicate<Tag> test, BiFunction<Mutable, T, Tag> mapper) {
            entries.add( new Entry(test, Either.right((BiFunction<Mutable, Tag, Tag>) mapper)) );
            return this;
        }

        public Tag map(Tag tag) {
            Either<Function<Tag, Tag>, BiFunction<Mutable, Tag, Tag>> mapping
                    = getMapping(tag);
            if(mapping != null) {
                Tag ntag;
                if(mapping.isLeft()) {
                    ntag = mapping.getLeft().apply(tag);
                } else {
                    ntag = mapping.getRight().apply(this, tag);
                }
                return ntag;
            } else {
                return tag;
            }
        }

        public Either<
                Function<Tag, Tag>,
                BiFunction<Mutable, Tag, Tag>> getMapping(Tag tag) {
            for(Entry e : entries) {
                if(e.test.test(tag)) {
                    return e.mapper;
                }
            }
            return null;
        }

        public MapTags immutable() {
            return new MapTags(this);
        }

        public static class Entry {
            private final Predicate<Tag> test;
            private final Either<
                    Function<Tag, Tag>,
                    BiFunction<Mutable, Tag, Tag>> mapper;
            public Entry(Predicate<Tag> test,
                         Either< Function<Tag, Tag>, BiFunction<Mutable, Tag, Tag> > mapper) {
                this.test = test;
                this.mapper = mapper;
            }
        }
    }

}
