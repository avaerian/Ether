package org.minerift.ether.config.types;

import org.minerift.ether.config.Config;
import org.minerift.ether.config.readers.IConfigReader;
import org.minerift.ether.config.readers.MainConfigReader;
import org.minerift.ether.config.readers.SchematicConfigReader;
import org.minerift.ether.config.writers.IConfigWriter;
import org.minerift.ether.config.writers.SchematicConfigWriter;

import java.io.File;
import java.util.function.Supplier;

public class ConfigType<T extends Config<T>> {

    public static final ConfigType<MainConfig> MAIN;
    public static final ConfigType<SchematicConfig> SCHEM_LIST;

    static {
        MAIN       = new ConfigType<>("MainConfig (config.yml)", MainConfig.class, new MainConfigReader(), null, MainConfig::new, null);
        SCHEM_LIST = new ConfigType<>("Schematic List (schems.yml)", SchematicConfig.class, new SchematicConfigReader(), new SchematicConfigWriter(), SchematicConfig::new, null);
    }

    private final String name;
    private final Class<T> typeClazz;
    private final IConfigReader<T> reader;
    private final IConfigWriter<T> writer;
    private final Supplier<T> defaultConfig;
    private final File file;

    private ConfigType(String name, Class<T> typeClazz, IConfigReader<T> reader, IConfigWriter<T> writer, Supplier<T> defaultConfig, File file) {
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

    public <P extends IConfigReader<T>> P getReader(Class<P> clazz) {
        final IConfigReader<T> reader = getReader();
        return reader == null ? null : clazz.cast(reader);
    }
}
