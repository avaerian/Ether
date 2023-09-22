package org.minerift.ether.config;

import org.minerift.ether.Ether;
import org.minerift.ether.config.exceptions.ConfigFileReadException;
import org.minerift.ether.config.types.ConfigType;

import java.io.FileNotFoundException;
import java.util.logging.Level;

public abstract class Config<T extends Config<T>> {

    public void save() {
        getType().getWriter().write((T) this);
    }

    // Loads from file again
    public void reload() {
        T reload;
        try {
            reload = getType().getReader().read(getType());
        } catch (FileNotFoundException ex) {
            reload = getType().getDefaultConfig();
        } catch (ConfigFileReadException ex) {
            // Config won't reload and log error to console for user to fix
            Ether.getLogger().log(Level.SEVERE, String.format("Failed to read %s", getType().getName()));
            ex.printStackTrace();
            return;
        }
        copyFrom(reload);
    }

    // Copies data from a similar config over to this config
    // This is used for reloading; a new config object will be created
    // with the new settings loaded, so we want to copy that data over
    // to the primary config object
    protected abstract void copyFrom(T other);

    public abstract ConfigType<T> getType();
}
