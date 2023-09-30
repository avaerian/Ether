package org.minerift.ether.schematic.readers;

import org.minerift.ether.schematic.SchematicFileReadException;
import org.minerift.ether.schematic.types.Schematic;
import org.minerift.ether.util.Result;

import java.io.File;

public interface ISchematicReader<S extends Schematic> {
    S read(File file) throws SchematicFileReadException;
}
