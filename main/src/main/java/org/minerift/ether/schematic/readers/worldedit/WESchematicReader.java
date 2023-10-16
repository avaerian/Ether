package org.minerift.ether.schematic.readers.worldedit;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import org.minerift.ether.schematic.SchematicFileReadException;
import org.minerift.ether.schematic.readers.ISchematicReader;
import org.minerift.ether.schematic.types.WorldEditSchematic;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class WESchematicReader implements ISchematicReader<WorldEditSchematic> {

    @Override
    public WorldEditSchematic read(File file) throws SchematicFileReadException {
        try {
            Clipboard clipboard = ClipboardFormats.findByFile(file).getReader(new FileInputStream(file)).read();
            return new WorldEditSchematic(clipboard);
        } catch (IOException ex) {
            throw (SchematicFileReadException) ex;
        }
    }

}
