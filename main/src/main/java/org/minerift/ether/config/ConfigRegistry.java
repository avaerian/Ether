package org.minerift.ether.config;

import org.minerift.ether.config.exceptions.ConfigFileReadException;
import org.minerift.ether.config.types.ConfigType;

import java.io.IOException;
import java.util.*;

// TODO: ConfigRegistry should only handle registry operations (registering + loading/reading, unloading/writing, unregistering)
public class ConfigRegistry {

    /**
     * For reading configs:
     *  If a config file exists, attempt to read and log exceptions (if any)
     *  If a config file doesn't exist, return default config object
     *
     * For writing configs:
     *  write() should handle all data dumping to storage
     *  It will always "create a new file" (replacing old configs) to store the data.
     *
     *
     *
     * FOR REGISTERING:
     * If a config file doesn't exist, load defined default (into memory)
     * After loading, write to file for users to interact with
     *
     * If a config file does exist, attempt to load file.
     */

    private final Map<ConfigType<?>, Config> configs;

    public ConfigRegistry() {
        this.configs = new HashMap<>();
    }

    // Register and load a config
    // Returns the config read from the file
    // If a config cannot be read, return the default config
    public <T extends Config<T>> T register(ConfigType<T> type) {
        T config;
        try {
            config = type.getReader().read(type);
        } catch (ConfigFileReadException ex) {
            // TODO: change this to throw the error and set the default config only if config doesn't exist
            config = type.getDefaultConfig();
        }
        configs.putIfAbsent(type, config);
        return config;
    }

    public <T extends Config<T>> T get(ConfigType<T> type) {
        final T config = (T) configs.get(type);
        if(config == null) {
            throw new IllegalArgumentException(String.format("Config type %s was not found!", type.getName()));
        }
        return config;
    }

    public Collection<Config> getAll() {
        return configs.values();
    }

    public Set<ConfigType<?>> getAllTypes() {
        return configs.keySet();
    }

}
