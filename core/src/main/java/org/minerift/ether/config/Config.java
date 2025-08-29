package org.minerift.ether.config;

import org.minerift.ether.Ether;
import org.minerift.ether.util.CanChange;

import java.io.FileNotFoundException;
import java.util.logging.Level;

public abstract class Config<T extends Config<T>> extends CanChange {

    public Config() {
        setChanged(false);
    }

    public void save() {
        try {
            getType().codec().write((T) this, getType().getFile());
        } catch (ConfigFileWriteException ex) {
            Ether.getLogger().log(Level.SEVERE, getType().getName() + " was unable to save: ", ex);
        }
    }

    public void saveIfChanged() {
        if(hasChanged()) {
            save();
        }
    }

    // Loads from file again
    // Returns whether the file reloaded successfully
    public boolean reload() {
        T reload;
        try {
            reload = getType().codec().read(getType());
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

    public String getName() {
        return getType().getName();
    }
}
