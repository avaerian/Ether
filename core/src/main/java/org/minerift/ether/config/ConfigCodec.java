package org.minerift.ether.config;

import org.minerift.ether.config.source.Source;
import org.minerift.ether.util.UnreachableException;

import static java.lang.String.format;

public abstract class ConfigCodec<T, S extends Source> {

    public final T read(S src) throws ConfigNotFoundException, ConfigReadException {
        if(!src.exists()) {
            throw new ConfigNotFoundException(/*src.getName() + */"*Config src* was not found");
        }
        return readIt(src);
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
    protected abstract T readIt(S src) throws ConfigReadException;

    @Deprecated
    public final void write(T config, S src) throws ConfigWriteException {
        // If a file doesn't exist, load default resource <- // should not be handled here as an edge-case


        if(file != null && !file.exists()) {
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
            } else*/ if (isDirectoryType() /*file.isDirectory()*/) {
                file.mkdirs(); // warn if mkdirs fails
            } else {
                throw new UnreachableException("Input is neither a file nor directory");
            }
        }

        // Once default resource is loaded, write changes to it
        writeIt(config, src);
    }

    // The file is guaranteed to exist for this method
    protected abstract void writeIt(T config, S src) throws ConfigWriteException;

}
