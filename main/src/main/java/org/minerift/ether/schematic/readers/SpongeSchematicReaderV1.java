package org.minerift.ether.schematic.readers;

import org.minerift.ether.schematic.types.SpongeSchematic;

public class SpongeSchematicReaderV1 implements SpongeSchematicReader {

    @Override
    public void readMetadata(SpongeSchematic.Builder builder) {

    }

    @Override
    public void readBlockStates(SpongeSchematic.Builder builder) {

    }

    @Override
    public void readBiomes(SpongeSchematic.Builder builder) {
        // Not supported
    }

    @Override
    public void readEntities(SpongeSchematic.Builder builder) {
        // Not supported
    }

    @Override
    public void readBlockEntities(SpongeSchematic.Builder builder) {

    }
}
