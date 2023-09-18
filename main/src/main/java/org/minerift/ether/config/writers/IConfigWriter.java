package org.minerift.ether.config.writers;

import org.minerift.ether.config.Config;

// Writes a Config object to its file
public interface IConfigWriter<T extends Config<?>> {
    void write(T config);
}
