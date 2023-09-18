package org.minerift.ether.config.readers;

import org.minerift.ether.config.Config;
import org.minerift.ether.config.exceptions.ConfigFileReadException;
import org.minerift.ether.config.types.ConfigType;

// Reads a file into a Config object
public interface IConfigReader<T extends Config<T>> {

    // Reads a config as an object
    // Throws an IOException if there are any issues with file access/IO
    // Throws a ConfigFileReadException if the config fails to read/parse
    T read(ConfigType<T> type) throws ConfigFileReadException;
}
