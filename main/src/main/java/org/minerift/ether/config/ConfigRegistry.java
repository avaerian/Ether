package org.minerift.ether.config;

import org.minerift.ether.Ether;
import org.minerift.ether.config.exceptions.ConfigFileReadException;
import org.minerift.ether.config.types.ConfigType;

import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

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

    // Attempts to register a config by loading it
    // Returns the config read from the file
    // If a config file doesn't exist, return the default config
    // If a config fails when reading, print the reader exception
    public <T extends Config<T>> T register(ConfigType<T> type) throws ConfigFileReadException {
        T config;
        try {
            config = type.getReader().read(type);
        } catch (FileNotFoundException ex) {
            config = type.getDefaultConfig();
        }
        configs.put(type, config);
        return config;
    }



    // Attempts to register a config by loading it
    // If the config fails to load, log the exception as a warning
    // TODO: reconsider refactoring as an overloaded method with a fail callback
    public <T extends Config<T>> T registerOrWarn(ConfigType<T> type) {
        try {
            return register(type);
        } catch (ConfigFileReadException ex) {
            Ether.getLogger().log(Level.WARNING, type.getName() + " was unable to load!");
            ex.printStackTrace();
            return null;
        }
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
