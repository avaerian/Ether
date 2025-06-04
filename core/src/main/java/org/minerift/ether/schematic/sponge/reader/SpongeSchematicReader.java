package org.minerift.ether.schematic.sponge.reader;

import org.minerift.ether.schematic.SchematicFileReadException;
import org.minerift.ether.schematic.SchematicReader;
import org.minerift.ether.schematic.sponge.reader.steps.*;
import org.minerift.ether.schematic.sponge.SpongeSchematic;

import java.io.File;

public class SpongeSchematicReader implements SchematicReader<SpongeSchematic> {

    public static class ReadStages {
        public final static IReaderStep INIT             = new ReadInitStep();
        public final static IReaderStep METADATA         = new ReadMetadataStep();
        public final static IReaderStep BLOCK_STATES     = new ReadBlockStatesStep();
        public final static IReaderStep BLOCK_ENTITIES   = new ReadBlockEntitiesStep();
        public final static IReaderStep ENTITIES         = new ReadEntitiesStep();
        public final static IReaderStep BIOMES           = new ReadBiomesStep();

        private ReadStages() {}
    }

    @Override
    public SpongeSchematic read(File file) throws SchematicFileReadException {

        final SchematicReaderContext ctx = SchematicReaderContext.from(file);

        ReadStages.INIT.read(ctx);
        ReadStages.METADATA.read(ctx);
        ReadStages.BLOCK_STATES.read(ctx);
        ReadStages.BLOCK_ENTITIES.read(ctx);
        ReadStages.BIOMES.read(ctx);
        ReadStages.ENTITIES.read(ctx);

        SpongeSchematic schem = ctx.builder.build();

        return schem;
    }
}
