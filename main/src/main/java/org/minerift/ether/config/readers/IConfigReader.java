package org.minerift.ether.config.readers;

import org.minerift.ether.config.Config;
import org.minerift.ether.config.exceptions.ConfigFileReadException;
import org.minerift.ether.config.types.ConfigType;

import java.io.FileNotFoundException;

// Reads a file into a Config object
public abstract class IConfigReader<T extends Config<T>> {

    // Convienence method
    protected void ensureFileExists(ConfigType<T> type) throws FileNotFoundException {
        if(!type.getFile().exists()) {
            throw new FileNotFoundException(type.getName() + " was not found!");
        }
    }

    // Reads a config as an object
    // Throws an IOException if there are any issues with file access/IO
    // Throws a ConfigFileReadException if the config fails to read/parse
    public abstract T read(ConfigType<T> type) throws FileNotFoundException, ConfigFileReadException;
}
