package org.minerift.ether.config;

import org.minerift.ether.config.exceptions.ConfigFileReadException;

import java.io.File;
import java.io.FileNotFoundException;

// Reads a file into a Config object
public abstract class IConfigReader<T extends Config<T>> {

    public T read(ConfigType<T> type) throws FileNotFoundException, ConfigFileReadException {
        final File file = type.getFile();

        // Ensure file exists for reading
        if(!file.exists()) {
            throw new FileNotFoundException(type.getName() + " was not found!");
        }

        return readIt(file);
    }

    // Reads a config as an object
    // File is guaranteed to exist at this point
    // Throws a ConfigFileReadException if the config fails to read/parse
    protected abstract T readIt(File file) throws ConfigFileReadException;
}
