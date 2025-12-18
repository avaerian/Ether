package org.minerift.ether.config;

import org.minerift.ether.config.islandspecs.IslandSpecsCodec;
import org.minerift.ether.config.islandspecs.IslandSpecsConfig;
import org.minerift.ether.config.main.MainConfig;
import org.minerift.ether.config.main.MainConfigCodec;
import org.minerift.ether.config.source.DirectorySource;
import org.minerift.ether.config.source.FileSource;
import org.minerift.ether.config.source.Source;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public final class ConfigType<T extends Config<T>, S extends Source> {

    public static final ConfigType<MainConfig, FileSource> MAIN;
    public static final ConfigType<IslandSpecsConfig, DirectorySource> ISLAND_SPECS_LIST;

    private static final AtomicInteger TYPE_ID_GEN = new AtomicInteger();

    static {
        MAIN = new ConfigType<>("MainConfig (config.yml)",
                MainConfig.class, MainConfigCodec.INST, MainConfig::new);
        ISLAND_SPECS_LIST = new ConfigType<>("Island Specs List (island_specs.yml)",
                IslandSpecsConfig.class, IslandSpecsCodec.INST, IslandSpecsConfig::new);
    }

    protected final int id;
    private final String name;
    private final Class<T> typeClazz;
    private final ConfigCodec<T, S> codec;
    private final Supplier<T> defaultConfig;

    // For every config type, a default resource file must exist (file cannot be null)
    public ConfigType(String name, Class<T> typeClazz, ConfigCodec<T, S> codec, Supplier<T> defaultConfig) {
        this.id = TYPE_ID_GEN.getAndIncrement();
        this.name = name;
        this.typeClazz = typeClazz;
        this.codec = codec;
        this.defaultConfig = defaultConfig;
    }

    public String getName() {
        return name;
    }

    public Class<T> getTypeClass() {
        return typeClazz;
    }

    public T getDefaultConfig() {
        return defaultConfig.get();
    }

    public ConfigCodec<T, S> codec() {
        return codec;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
