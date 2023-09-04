package org.minerift.ether.schematic.readers.worldedit;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import org.minerift.ether.schematic.SchematicFileReadException;
import org.minerift.ether.schematic.readers.ISchematicReader;
import org.minerift.ether.schematic.types.WorldEditSchematic;
import org.minerift.ether.util.Result;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class WESchematicReader implements ISchematicReader<WorldEditSchematic> {

    @Override
    public Result<WorldEditSchematic, SchematicFileReadException> read(File file) {

        final Result<WorldEditSchematic, SchematicFileReadException> result = new Result<>();

        try {
            Clipboard clipboard = ClipboardFormats.findByFile(file).getReader(new FileInputStream(file)).read();
            result.ok(new WorldEditSchematic(clipboard));
        } catch (IOException ex) {
            result.err((SchematicFileReadException) ex);
        }

        return result;
    }

}
