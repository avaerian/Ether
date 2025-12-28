package org.minerift.ether.config;

import org.minerift.ether.config.source.Source;

import java.io.IOException;

public abstract class ConfigCodec<T extends Config<T>, S extends Source> {

    public final void read(T cfg, S src) throws ConfigNotFoundException, ConfigReadException {
        if(!src.exists()) {
            throw new ConfigNotFoundException(/*src.getName() + */"*Config src* was not found");
        }

        try {
            cfg.readLock().lock();
            readIt(cfg, src);
        } finally {
            cfg.readLock().unlock();
        }
    }

    // Reads a config as an object
    // File is guaranteed to exist at this point
    // Throws a ConfigFileReadException if the config fails to read/parse
    protected abstract void readIt(T cfg, S src) throws ConfigReadException;

    public final void write(T cfg, S src) throws ConfigWriteException {
        // If a file doesn't exist, load default resource <- // should not be handled here as an edge-case
        /*if(file != null && !file.exists()) {
            /*if(isFileType()) {
                try {
                    InputStream res = EtherPlugin.class.getResource(file.getName()).openStream(); // REVIEW

                    // Create dirs + file
                    Files.createParentDirs(file);
                    file.createNewFile();

                    // Write default resource to file
                    FileOutputStream out = new FileOutputStream(file);
                    out.write(res.readAllBytes());
                    out.close();
                } catch (IOException ex) {
                    throw new ConfigFileWriteException("Failed to write data to file", ex);
                }
            } else if (isDirectoryType() file.isDirectory()) {
                file.mkdirs(); // warn if mkdirs fails
            } else {
                throw new UnreachableException("Input is neither a file nor directory");
            }
        }*/

        try {
            src.createIfNotExists();
        } catch (IOException e) {
            throw new ConfigWriteException("Failed to create config source", e);
        }

        try {
            cfg.writeLock().lock();
            writeIt(cfg, src);
        } finally {
            cfg.writeLock().unlock();
        }
    }

    // The src is guaranteed to exist for this method
    protected abstract void writeIt(T cfg, S src) throws ConfigWriteException;

}
