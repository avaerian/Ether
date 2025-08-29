package org.minerift.ether.config;

import com.google.common.io.Files;
import org.minerift.ether.Ether;
import org.minerift.ether.util.UnreachableException;

import java.io.*;

import static java.lang.String.format;

public abstract class ConfigCodec<T extends Config<T>> {

    protected static final int NO_FLAGS = 0;
    protected static final int ALLOWS_NULL_FILE = 1;

    private final int flags;

    protected ConfigCodec(int flags) {
        this.flags = flags;
    }

    public final T read(ConfigType<T> type) throws FileNotFoundException, ConfigFileReadException {
        final File file = type.getFile();

        // If the file isn't null, ensure it exists
        if(file != null && !file.exists()) {
            throw new FileNotFoundException(type.getName() + " was not found!");
        }

        return readIt(file);
    }

    // Reads a config as an object
    // File is guaranteed to exist at this point
    // Throws a ConfigFileReadException if the config fails to read/parse
    protected abstract T readIt(File file) throws ConfigFileReadException;

    // FIXME: refactor to allow file to be a directory
    public final void write(T config, File file) throws ConfigFileWriteException {
        if(file == null && (flags & ALLOWS_NULL_FILE) == 0) {
            throw new ConfigFileWriteException(format("Provided file is null; %s disallows null files", config.getType().getName()));
        }

        // If a file doesn't exist, load default resource
        if(file != null && !file.exists()) {
            if(file.isFile()) {
                InputStream res = Ether.plugin().getResource(file.getName());
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
            } else if (file.isDirectory()) {
                // TODO
            } else {
                throw new UnreachableException("Input is neither a file nor directory");
            }
        }

        // Once default resource is loaded, write changes to it
        writeIt(config, file);
    }

    // The file is guaranteed to exist for this method
    protected abstract void writeIt(T config, File file) throws ConfigFileWriteException;

}
