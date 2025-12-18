package org.minerift.ether.config.source;

import java.io.File;

public class DirectorySource implements Source {

    public static DirectorySource of(File dir) {
        if(!dir.isDirectory()) {
            throw new IllegalArgumentException("File must be a directory");
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

    @Override
    public boolean create() {
        return false;
    }

    @Override
    public boolean exists() {
        return false;
    }
}
