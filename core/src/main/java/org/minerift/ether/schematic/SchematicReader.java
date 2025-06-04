package org.minerift.ether.schematic;

import java.io.File;

public interface SchematicReader<S extends Schematic> {
    S read(File file) throws SchematicFileReadException;
}
