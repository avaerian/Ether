package org.minerift.ether.schematic.readers.sponge.steps;

import org.minerift.ether.schematic.readers.sponge.ReaderContext;
import org.minerift.ether.schematic.SchematicFileReadException;

public interface IReaderStep {
    void read(ReaderContext ctx) throws SchematicFileReadException;
}
