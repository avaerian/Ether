package org.minerift.ether.config.source;

import java.io.File;
import java.io.IOException;

public class DirectorySource implements Source {

    public static DirectorySource of(File dir) throws IOException {
        if(dir.exists() && !dir.isDirectory()) {
            throw new IOException(dir.getPath() + " is not a directory");
        }
        return new DirectorySource(dir);
    }

    private File dir;

    public DirectorySource(File dir) {
        this.dir = dir;
    }

    public File getDirectory() {
        return dir;
    }

    // creates parent dirs
    @Override
    public boolean create() {
        return dir.mkdirs();
    }

    @Override
    public boolean exists() {
        return dir.exists();
    }
}
