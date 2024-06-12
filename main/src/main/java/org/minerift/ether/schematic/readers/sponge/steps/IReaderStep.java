package org.minerift.ether.schematic.readers.sponge.steps;

import org.minerift.ether.schematic.readers.sponge.SchematicReaderContext;
import org.minerift.ether.schematic.SchematicFileReadException;

public interface IReaderStep {
    void read(SchematicReaderContext ctx) throws SchematicFileReadException;
}
