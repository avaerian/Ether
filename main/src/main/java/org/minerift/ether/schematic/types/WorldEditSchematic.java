package org.minerift.ether.schematic.types;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import org.minerift.ether.schematic.Schematic;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class WorldEditSchematic implements Schematic {

    private Clipboard clipboard;

    public WorldEditSchematic(File file) throws IOException {
        this.clipboard = ClipboardFormats.findByFile(file).getReader(new FileInputStream(file)).read();
    }

    public Clipboard getClipboard() {
        return clipboard;
    }

    @Override
    public int getWidth() {
        return clipboard.getDimensions().getX();
    }

    @Override
    public int getHeight() {
        return clipboard.getDimensions().getY();
    }

    @Override
    public int getLength() {
        return clipboard.getDimensions().getZ();
    }
}
