package org.minerift.ether.schematic.readers;

import org.minerift.ether.schematic.SchematicFileReadException;
import org.minerift.ether.schematic.types.Schematic;

import java.io.File;

public interface ISchematicReader<S extends Schematic> {
    S read(File file) throws SchematicFileReadException;
}
