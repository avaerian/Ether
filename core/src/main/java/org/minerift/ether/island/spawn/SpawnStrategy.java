package org.minerift.ether.island.spawn;

import org.jetbrains.annotations.Nullable;
import org.minerift.ether.debug.NeedsTesting;
import org.minerift.ether.user.EtherUser;
import org.minerift.ether.util.nbt.NbtSerializable;
import org.minerift.ether.util.nbt.tags.Tag;
import org.minerift.ether.util.nbt.tags.TagType;
import org.minerift.ether.world.Location;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@NeedsTesting
public abstract class SpawnStrategy implements NbtSerializable {

    public static final Registry REGISTRY = new Registry();

    @NeedsTesting
    public static class Registry {
        private final ConcurrentHashMap<Class<? extends SpawnStrategy>, RegEntry<?, ?>> data;

        protected Registry() {
            this.data = new ConcurrentHashMap<>();
        }

        protected Registry(ConcurrentHashMap<Class<? extends SpawnStrategy>, RegEntry<?, ?>> data) {
            this.data = data;
        }

        /*public boolean register(Class<? extends SpawnStrategy> clazz,
                                       Set<TagType<?>> tagTypes, Function<Tag, SpawnStrategy> loader) {
            return register(clazz, new RegInfo(tagTypes, loader));
        }*/

        public <T extends SpawnStrategy> boolean register(Class<T> clazz, RegEntry<?, T> info) {
            RegEntry<?, ?> old = data.putIfAbsent(clazz, info);
            return old == null;
        }

        public <T extends SpawnStrategy> Optional<RegEntry<?, T>> tryGet(Class<T> clazz) {
            return Optional.ofNullable( (RegEntry<?, T>) (data.get(clazz)) );
        }

        public <T extends SpawnStrategy> @Nullable RegEntry<?, T> get(Class<T> clazz) {
            return (RegEntry<?, T>) data.get(clazz);
        }

        public <T extends SpawnStrategy> RegEntry<?, T> getOrThrow(Class<T> clazz) throws SpawnStrategyLoadException {
            RegEntry<?, T> info = (RegEntry<?, T>) data.get(clazz);
            if(info == null) {
                throw new SpawnStrategyLoadException("No spawn strategy found of class " + clazz);
            }
            return info;
        }
    }

    public static class RegEntry<TT extends Tag, S extends SpawnStrategy> {
        private final Set<TagType<?>> types;
        private final LoaderFn<TT, S> loader;

        public RegEntry(Set<TagType<?>> tagTypes, LoaderFn<TT, S> loader) {
            this.types = Collections.unmodifiableSet(tagTypes);
            this.loader = loader;
        }

        public S load(Tag nbt) throws SpawnStrategyLoadException {
            if(!types.contains(nbt.type())) {
                throw new SpawnStrategyLoadException("Expected a tag of type " + types + ", found " + nbt.type());
            }
            return loader.apply((TT)nbt);
        }
    }

    @FunctionalInterface
    public interface LoaderFn<T extends Tag, S extends SpawnStrategy> {
        S apply(T tag) throws SpawnStrategyLoadException;
    }

    protected static <T extends SpawnStrategy> boolean register(Class<T> clazz,
                                   Set<TagType<?>> types, LoaderFn<Tag, T> loader) {
        return REGISTRY.register(clazz, new RegEntry<>(types, loader));
    }

    protected static <TT extends Tag, T extends SpawnStrategy> boolean register(Class<T> clazz,
                                                                             TagType<TT> type, LoaderFn<TT, T> loader) {
        return REGISTRY.register(clazz, new RegEntry<>(Set.of(type), loader));
    }

    protected static <T extends SpawnStrategy> boolean register(Class<T> clazz, RegEntry<Tag, T> info) {
        return REGISTRY.register(clazz, info);
    }

    public static <T extends SpawnStrategy> T of(Class<T> clazz, Tag nbt) throws SpawnStrategyLoadException {
        return REGISTRY.getOrThrow(clazz).load(nbt);
    }

    // return the location the user spawned at
    public abstract Location spawn(EtherUser user);

    public abstract String getTypeName();

}
