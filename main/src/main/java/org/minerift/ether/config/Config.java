package org.minerift.ether.config;

import org.minerift.ether.Ether;
import org.minerift.ether.config.exceptions.ConfigFileReadException;
import org.minerift.ether.config.exceptions.ConfigFileWriteException;

import java.io.FileNotFoundException;
import java.util.logging.Level;

public abstract class Config<T extends Config<T>> {

    private boolean hasChanged;

    public Config() {
        this.hasChanged = false;
    }

    public void save() {
        try {
            getType().getWriter().write((T) this, getType().getFile());
        } catch (ConfigFileWriteException ex) {
            Ether.getLogger().log(Level.SEVERE, getType().getName() + " was unable to save: ", ex);
        }
    }

    public void saveIfChanged() {
        if(hasChanged) {
            save();
        }
    }

    // Loads from file again
    // Returns whether the file reloaded successfully
    public boolean reload() {
        T reload;
        try {
            reload = getType().getReader().read(getType());
        } catch (FileNotFoundException ex) {
            reload = getType().getDefaultConfig();
        } catch (ConfigFileReadException ex) {
            // Config won't reload and log error to console for user to fix
            Ether.getLogger().log(Level.SEVERE, String.format("Failed to read %s when reloading!", getType().getName()), ex);
            return false;
        }
        reload.save(); // once config has verified/loaded data, save verified data
        copyFrom(reload);
        return true;
    }

    // Copies data from a similar config over to this config
    // This is used for reloading; a new config object will be created
    // with the new settings loaded, so we want to copy that data over
    // to the primary config object
    protected abstract void copyFrom(T other);

    public abstract ConfigType<T> getType();

    // Must be applied to any setters or data modifying methods
    public void setChanged(boolean changed) {
        this.hasChanged = changed;
    }

    public boolean hasChanged() {
        return hasChanged;
    }
}
