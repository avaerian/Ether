package org.minerift.ether.schematic.sponge.reader.steps;

import org.minerift.ether.schematic.sponge.reader.SchematicReaderContext;
import org.minerift.ether.schematic.SchematicReadException;

public interface IReaderStep {
    void read(SchematicReaderContext ctx) throws SchematicReadException;
}
