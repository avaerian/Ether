package org.minerift.ether.schematic.readers.sponge;

import org.minerift.ether.schematic.SchematicFileReadException;
import org.minerift.ether.schematic.readers.ISchematicReader;
import org.minerift.ether.schematic.readers.sponge.steps.*;
import org.minerift.ether.schematic.types.DeprecatedSpongeSchematic;

import java.io.File;
import java.io.IOException;

public class SpongeSchematicReader implements ISchematicReader<DeprecatedSpongeSchematic> {

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
    public DeprecatedSpongeSchematic read(File file) throws SchematicFileReadException {

        final SchematicReaderContext ctx = SchematicReaderContext.from(file);

        ReadStages.INIT.read(ctx);
        ReadStages.METADATA.read(ctx);
        ReadStages.BLOCK_ENTITIES.read(ctx);
        ReadStages.BLOCK_STATES.read(ctx);
        ReadStages.BIOMES.read(ctx);
        ReadStages.ENTITIES.read(ctx);

        DeprecatedSpongeSchematic schem = ctx.builder.build();

        try {
            ctx.close();
        } catch (IOException ex) {
            // Context failed to close; this should be notified
            throw new RuntimeException("Failed to close schematic reader context!", ex);
        }

        return schem;
    }
}
