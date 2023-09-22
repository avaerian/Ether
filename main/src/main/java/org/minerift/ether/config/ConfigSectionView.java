package org.minerift.ether.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.minerift.ether.util.Result;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

// Wrapper class for the YamlConfiguration Bukkit API
public class ConfigSectionView {

    private final ConfigurationSection head;

    public static Result<ConfigSectionView, IOException> from(File file) {

        final Result<ConfigSectionView, IOException> result = new Result<>();

        // Attempt to load config file
        final YamlConfiguration bukkitView = new YamlConfiguration();
        try {
            bukkitView.load(file);
            result.ok(new ConfigSectionView(bukkitView));
        } catch (IOException ex) {
            result.err(ex);
        } catch (InvalidConfigurationException ex) {
            result.err(new IOException(ex.getMessage()));
        }

        return result;
    }

    private ConfigSectionView(ConfigurationSection head) {
        this.head = head;
    }

    public Optional<Object> get(String path) {
        return Optional.ofNullable(head.get(path, null));
    }

    public Optional<ConfigSectionView> getSectionView(String path) {
        final ConfigurationSection section = head.getConfigurationSection(path);
        return Optional.ofNullable(section == null ? null : new ConfigSectionView(section));
    }

    // TODO: create get() method with an adapter parameter

    public ConfigurationSection getBukkitView() {
        return head;
    }

}
