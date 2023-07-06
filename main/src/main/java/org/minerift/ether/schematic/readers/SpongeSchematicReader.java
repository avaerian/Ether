package org.minerift.ether.schematic.readers;

import org.minerift.ether.schematic.types.SpongeSchematic;

public interface SpongeSchematicReader {

    void readMetadata(SpongeSchematic.Builder builder);
    void readBlockStates(SpongeSchematic.Builder builder);
    void readBiomes(SpongeSchematic.Builder builder);
    void readEntities(SpongeSchematic.Builder builder);
    void readBlockEntities(SpongeSchematic.Builder builder);

}
