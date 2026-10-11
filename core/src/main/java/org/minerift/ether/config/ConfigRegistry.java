package org.minerift.ether.config;

import org.jetbrains.annotations.NotNull;
import org.minerift.ether.config.source.Source;
import org.minerift.ether.util.collect.Int2ObjectIdentityMap;
import org.slf4j.Logger;

import java.io.File;
import java.util.*;

// ConfigRegistry should only handle registry operations (registering + loading/reading, unloading/writing, unregistering)
public class ConfigRegistry implements Iterable<ConfigRegistry.Entry> {

    protected final File dir;
    protected final Logger logger;

    protected final Int2ObjectIdentityMap<ConfigType<?,?>> types; // purely to store types
    protected final Int2ObjectIdentityMap<Config> cfgs;
    protected final Int2ObjectIdentityMap<Source> srcs;
    protected final BitSet updatedCfgs;
    //private

    public ConfigRegistry(File dir, Logger logger) {
        this.dir = dir;
        this.logger = logger;
        this.types = new Int2ObjectIdentityMap<>();
        this.cfgs = new Int2ObjectIdentityMap<>();
        this.srcs = new Int2ObjectIdentityMap<>();
        this.updatedCfgs = new BitSet();
    }

    // Attempts to register a config by loading it
    // Returns the config read from the file
    // If a config file doesn't exist, return the default config
    // If a config fails when reading, delegate exception to user
    public <T extends Config<T>, S extends Source> T register(ConfigType<T, S> type, S src) throws ConfigReadException {
        T cfg = type.getDefaultConfig(this, src);
        try {
            type.codec().read(cfg, src);
        } catch (ConfigNotFoundException ex) {
            logger.warn("Using default config", ex);
            cfg = type.getDefaultConfig(this, src);
        }
        types.put(type.id, type);
        cfgs.put(type.id, cfg);
        srcs.put(type.id, src);
        return cfg;
    }

    /*public <T extends Config<T>, S extends Source> T register(ConfigType<T, S> type, Config.SourceSupplier<S> srcSupplier) throws ConfigReadException {
        S src;
        try {
            src = srcSupplier.create();
        } catch (Exception e) {
            throw new ConfigReadException("Failed to open source", e);
        }
        return register(type, src);
    }*/

    // Attempts to register a config by loading it
    // If the config fails to load, log the exception as a warning; the config will need to be registered again
    // If the config fails to load, return null
    public <T extends Config<T>, S extends Source> T registerOrWarn(ConfigType<T, S> type, S src) {
        try {
            return register(type, src);
        } catch (ConfigReadException ex) {
            logger.warn(type.getName() + " was unable to load", ex);
            return null;
        }
    }

    public <T extends Config<T>> T get(ConfigType<T, ?> type) {
        final T config = (T) cfgs.get(type.id);
        if(config == null) {
            throw new IllegalArgumentException(String.format("Config type %s was not found", type.getName()));
        }
        return config;
    }

    public Collection<Config> getAll() {
        return cfgs.values();
    }

    public Set<ConfigType<?, ?>> getAllTypes() {
        return new HashSet<>(types.values());
    }

    @Override
    public @NotNull Iterator<Entry> iterator() {
        return new Iterator<>() {
            int id = 0;

            @Override
            public boolean hasNext() {
                return id < types.size();
            }

            @Override
            public Entry next() {
                Entry entry = new Entry(id, types.get(id), cfgs.get(id));
                id++;
                return entry;
            }
        };
    }

    public static class Entry {
        public final int id;
        public final ConfigType<?, ?> type;
        public final Config config;

        public Entry(int id, ConfigType<?, ?> type, Config<?> config) {
            this.id = id;
            this.type = type;
            this.config = config;
        }
    }
}
