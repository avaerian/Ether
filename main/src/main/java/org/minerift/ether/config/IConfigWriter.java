package org.minerift.ether.config;

import com.google.common.io.Files;
import org.minerift.ether.Ether;
import org.minerift.ether.config.exceptions.ConfigFileWriteException;

import java.io.*;

// Writes a Config object to its file
public abstract class IConfigWriter<T extends Config<T>> {

    public void write(T config, File file) throws ConfigFileWriteException {
        // If a file doesn't exist, load default resource
        if(!file.exists()) {
            InputStream res = Ether.getPlugin().getResource(file.getName());
            try {
                // Create dirs + file
                Files.createParentDirs(file);
                file.createNewFile();

                // Write default resource to file
                FileOutputStream out = new FileOutputStream(file);
                out.write(res.readAllBytes());
                out.close();
            } catch (IOException ex) {
                throw new ConfigFileWriteException("Failed to write data to file!", ex);
            }
        }

        // Once default resource is loaded, write changes to it
        writeIt(config, file);
    }

    // The file is guaranteed to exist for this method
    protected abstract void writeIt(T config, File file) throws ConfigFileWriteException;
}
