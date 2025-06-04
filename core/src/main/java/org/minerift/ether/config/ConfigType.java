package org.minerift.ether.config;

import org.minerift.ether.Ether;
import org.minerift.ether.config.islandspecs.IslandSpecsConfig;
import org.minerift.ether.config.islandspecs.IslandSpecsReader;
import org.minerift.ether.config.islandspecs.IslandSpecsWriter;
import org.minerift.ether.config.main.MainConfig;
import org.minerift.ether.config.main.MainConfigReader;
import org.minerift.ether.config.main.MainConfigWriter;

import java.io.File;
import java.util.function.Supplier;

public class ConfigType<T extends Config<T>> {

    public static final ConfigType<MainConfig> MAIN;
    public static final ConfigType<IslandSpecsConfig> ISLAND_SPECS_LIST;

    static {
        MAIN       = new ConfigType<>("MainConfig (config.yml)", MainConfig.class, new MainConfigReader(), new MainConfigWriter(), MainConfig::new, Ether.getPluginFile("config.yml"));
        ISLAND_SPECS_LIST = new ConfigType<>("Island Specs List (island_specs.yml)", IslandSpecsConfig.class, new IslandSpecsReader(), new IslandSpecsWriter(), IslandSpecsConfig::new, Ether.getPluginFile("island_specs"));
    }

    private final String name;
    private final Class<T> typeClazz;
    private final IConfigReader<T> reader;
    private final IConfigWriter<T> writer;
    private final Supplier<T> defaultConfig;
    private final File file;

    // For every config type, a default resource file must exist (file cannot be null)
    public ConfigType(String name, Class<T> typeClazz, IConfigReader<T> reader, IConfigWriter<T> writer, Supplier<T> defaultConfig, File file) {
        this.name = name;
        this.typeClazz = typeClazz;
        this.reader = reader;
        this.writer = writer;
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

    public IConfigReader<T> getReader() {
        return reader;
    }

    public IConfigWriter<T> getWriter() {
        return writer;
    }

    public File getFile() {
        return file;
    }
}
