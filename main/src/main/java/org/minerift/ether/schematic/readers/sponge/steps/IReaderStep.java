package org.minerift.ether.schematic.readers.sponge.steps;

import org.minerift.ether.schematic.readers.sponge.ReaderContext;
import org.minerift.ether.schematic.SchematicFileReadException;

public interface IReaderStep {

    // Returns whether the data was successfully read or not
    void read(ReaderContext ctx) throws SchematicFileReadException;

}
