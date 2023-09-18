package org.minerift.ether.config;

import org.minerift.ether.Ether;
import org.minerift.ether.EtherPlugin;
import org.minerift.ether.config.exceptions.ConfigFileReadException;
import org.minerift.ether.config.types.ConfigType;

import java.io.File;
import java.util.logging.Level;

public abstract class Config<T extends Config<T>> {

    public File getPluginDirectory() {
        return EtherPlugin.getInstance().getDataFolder();
    }

    public void save() {
        getType().getWriter().write((T) this);
    }

    public void reload() {
        // Loads from file again
        try {
            T reload = getType().getReader().read(getType());
            copyFrom(reload);
        } catch (ConfigFileReadException ex) {
            Ether.getLogger().log(Level.SEVERE, String.format("Failed to read %s", getType().getName()));
            ex.printStackTrace();
        }
    }

    // Copies data from a similar config over to this config
    protected abstract void copyFrom(T other);

    public abstract ConfigType<T> getType();
}
