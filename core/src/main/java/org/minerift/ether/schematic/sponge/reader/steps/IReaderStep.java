package org.minerift.ether.schematic.sponge.reader.steps;

import org.minerift.ether.schematic.sponge.reader.SchematicReaderContext;
import org.minerift.ether.schematic.SchematicFileReadException;

public interface IReaderStep {
    void read(SchematicReaderContext ctx) throws SchematicFileReadException;
}
