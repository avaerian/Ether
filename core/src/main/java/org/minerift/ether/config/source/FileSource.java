package org.minerift.ether.config.source;

import org.minerift.ether.config.ConfigReadException;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.OpenOption;
import java.nio.file.Path;

public class FileSource implements Source {

    // validated
    public static FileSource of(File file) throws IOException {
        // TODO: manage this shit via FileChannel and FileLock locks
        //FileLock lock = FileChannel.open()
        if(!file.createNewFile()) {
            throw new IOException("File already exists (" + file + ")");
        }
        return new FileSource(file);
    }

    private final File file;

    public FileSource(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    public Path getFilePath() {
        return file.toPath();
    }

    public Path getFileName() {
        return file.toPath().getFileName();
    }

    public FileChannel openFileChannel(OpenOption... options) throws IOException {
        return FileChannel.open(file.toPath(), options);
    }

    // in this case, returns true if file is created, and false if file already exists and wasn't created
    @Override
    public boolean create() throws IOException {
        return file.createNewFile();
    }

    @Override
    public boolean exists() {
        return file.exists();
    }
}