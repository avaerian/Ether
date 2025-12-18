package org.minerift.ether.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

// Wrapper class for the YamlConfiguration Bukkit API
// TODO: desirable to decouple from Bukkit for config unit testing
public class YamlConfigView {
    private final YamlConfiguration yamlConfig;
    private final ConfigurationSection head;

    // Load a specific yaml file as a view
    public static YamlConfigView from(File file) throws IOException {

        // Attempt to load config file
        final YamlConfiguration bukkitView = new YamlConfiguration();
        try {
            bukkitView.load(file);
            return new YamlConfigView(bukkitView);
        } catch (InvalidConfigurationException ex) {
            throw new IOException(ex.getMessage());
        }
    }

    // View of a specific section in a config
    private YamlConfigView(YamlConfiguration yamlConfig, ConfigurationSection head) {
        this.yamlConfig = yamlConfig;
        this.head = head;
    }

    // Root section view of config
    private YamlConfigView(YamlConfiguration yamlConfig) {
        this(yamlConfig, yamlConfig);
    }

    public <T> Optional<T> get(Class<T> expectedClazz, String path) throws ConfigReadException {
        final Object val = head.get(path, null);
        if(val == null) {
            return Optional.empty();
        }

        if(val.getClass().isAssignableFrom(expectedClazz)) { // TODO: change to expectedClazz.isInstance(val) ?
            return Optional.of(expectedClazz.cast(val));
        }
        throw new ConfigReadException(String.format("Expected type %s for path %s, got type %s", expectedClazz.getName(), path, val.getClass().getName()));
    }

    public void set(String path, Object obj) {
        Object existing = head.get(path, null);
        if(existing != null && existing.equals(obj)) {
            return;
        }
        head.set(path, obj);
    }

    // Save the yaml config to the specified file
    public void save(File file) throws ConfigWriteException {
        try {
            yamlConfig.save(file);
        } catch (IOException ex) {
            throw new ConfigWriteException("Failed to save config", ex);
        }
    }

    public Optional<YamlConfigView> getSectionView(String path) {
        final ConfigurationSection section = head.getConfigurationSection(path);
        return Optional.ofNullable(section == null ? null : new YamlConfigView(yamlConfig, section));
    }

    public ConfigurationSection getBukkitView() {
        return head;
    }
}
