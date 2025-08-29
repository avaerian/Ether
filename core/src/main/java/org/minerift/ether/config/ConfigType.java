package org.minerift.ether.config;

import org.minerift.ether.Ether;
import org.minerift.ether.config.islandspecs.IslandSpecsConfig;
import org.minerift.ether.config.main.MainConfig;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public final class ConfigType<T extends Config<T>> {

    public static final ConfigType<MainConfig> MAIN;
    public static final ConfigType<IslandSpecsConfig> ISLAND_SPECS_LIST;

    private static final AtomicInteger TYPE_ID_GEN = new AtomicInteger();

    static {
        MAIN = new ConfigType<>("MainConfig (config.yml)",
                MainConfig.class, MainConfig.CODEC, MainConfig::new, Ether.getPluginFile("config.yml"));
        ISLAND_SPECS_LIST = new ConfigType<>("Island Specs List (island_specs.yml)",
                IslandSpecsConfig.class, IslandSpecsConfig.CODEC, IslandSpecsConfig::new, Ether.getPluginFile("island_specs"));
    }

    protected final int id;
    private final String name;
    private final Class<T> typeClazz;
    private final ConfigCodec<T> codec;
    private final Supplier<T> defaultConfig;
    private final File file;

    // For every config type, a default resource file must exist (file cannot be null)
    public ConfigType(String name, Class<T> typeClazz, ConfigCodec<T> codec, Supplier<T> defaultConfig, File file) {
        this.id = TYPE_ID_GEN.getAndIncrement();
        this.name = name;
        this.typeClazz = typeClazz;
        this.codec = codec;
        this.defaultConfig = defaultConfig;
        this.file = file;
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

    public ConfigCodec<T> codec() {
        return codec;
    }

    public File getFile() {
        return file;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
