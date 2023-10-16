package org.minerift.ether.config;

import org.minerift.ether.Ether;
import org.minerift.ether.config.main.MainConfig;
import org.minerift.ether.config.main.MainConfigReader;
import org.minerift.ether.config.main.MainConfigWriter;
import org.minerift.ether.config.schems.SchematicsConfig;
import org.minerift.ether.config.schems.SchematicsConfigReader;
import org.minerift.ether.config.schems.SchematicsConfigWriter;

import java.io.File;
import java.util.function.Supplier;

public class ConfigType<T extends Config<T>> {

    public static final ConfigType<MainConfig> MAIN;
    public static final ConfigType<SchematicsConfig> SCHEM_LIST;

    static {
        MAIN       = new ConfigType<>("MainConfig (config.yml)", MainConfig.class, new MainConfigReader(), new MainConfigWriter(), MainConfig::new, new File(Ether.getPluginDir(), "config.yml"));
        SCHEM_LIST = new ConfigType<>("Schematic List (schem_list.yml)", SchematicsConfig.class, new SchematicsConfigReader(), new SchematicsConfigWriter(), SchematicsConfig::new, null);
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
