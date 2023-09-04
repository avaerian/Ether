package org.minerift.ether.schematic.readers.sponge;

import org.minerift.ether.schematic.readers.sponge.steps.*;

public class ReadStages {

    public final static IReaderStep INIT             = new ReadInitStep();
    public final static IReaderStep METADATA         = new ReadMetadataStep();
    public final static IReaderStep BLOCK_STATES     = new ReadBlockStatesStep();
    public final static IReaderStep BLOCK_ENTITIES   = new ReadBlockEntitiesStep();
    public final static IReaderStep ENTITIES         = new ReadEntitiesStep();
    public final static IReaderStep BIOMES           = new ReadBiomesStep();

    private ReadStages() {}

}
