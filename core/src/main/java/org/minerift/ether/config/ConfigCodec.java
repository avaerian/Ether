package org.minerift.ether.config;

import com.google.common.io.Files;
import org.minerift.ether.Ether;
import org.minerift.ether.util.UnreachableException;

import java.io.*;

import static java.lang.String.format;

public abstract class ConfigCodec<T extends Config<T>> {

    protected static final int NO_FLAGS = 0;
    protected static final int ALLOWS_NULL_SRC = 1;

    // TODO: implement Source interface and enums? (FileSystemSrc)
    protected static final int TYPE_FILE = 2; // temp
    protected static final int TYPE_DIR = 4; // temp
    protected static final int TYPES_MASK = TYPE_FILE | TYPE_DIR; // temp
    //protected static final int TYPE_OUT_OF_BOX = 8;

    protected final int flags;

    protected ConfigCodec(int flags) {
        if((flags & TYPES_MASK) == TYPES_MASK) { // both or all types flagged
            // TODO: logger;
            System.out.println("WARNING: cfg registered with all types flagged");
        }
        this.flags = flags;
    }

    boolean isFileType() {
        return (flags & TYPE_FILE) != 0;
    }

    boolean isDirectoryType() {
        return (flags & TYPE_DIR) != 0;
    }

    public final T read(ConfigType<T> type) throws FileNotFoundException, ConfigFileReadException {
        final File file = type.getFile();

        // If the file isn't null, ensure it exists
        if(file != null && !file.exists()) {
            throw new FileNotFoundException(type.getName() + " was not found");
        }

        return readIt(file);
    }

    /*@Debug
    public static void main(String[] args) throws URISyntaxException, IOException {
        BasicFileAttributes attrs = java.nio.file.Files
            .readAttributes(new File("C:\\tests").toPath(), BasicFileAttributes.class);
        System.out.println(attrs.isDirectory());
    }*/

    // Reads a config as an object
    // File is guaranteed to exist at this point
    // Throws a ConfigFileReadException if the config fails to read/parse
    protected abstract T readIt(File file) throws ConfigFileReadException;

    public final void write(T config, File file) throws ConfigFileWriteException {
        if(file == null && (flags & ALLOWS_NULL_SRC) == 0) {
            throw new ConfigFileWriteException(format("Provided file is null; %s disallows null files", config.getType().getName()));
        }

        // If a file doesn't exist, load default resource
        if(file != null && !file.exists()) {
            if(isFileType() /*file.isFile()*/) {
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
                    throw new ConfigFileWriteException("Failed to write data to file", ex);
                }
            } else if (isDirectoryType() /*file.isDirectory()*/) {
                file.mkdirs(); // warn if mkdirs failed
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
