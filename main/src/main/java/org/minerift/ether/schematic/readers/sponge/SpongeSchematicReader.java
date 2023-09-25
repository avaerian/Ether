package org.minerift.ether.schematic.readers.sponge;

import org.minerift.ether.schematic.SchematicFileReadException;
import org.minerift.ether.schematic.readers.ISchematicReader;
import org.minerift.ether.schematic.types.SpongeSchematic;

import java.io.File;
import java.io.IOException;

public class SpongeSchematicReader implements ISchematicReader<SpongeSchematic> {

    @Override
    public SpongeSchematic read(File file) throws SchematicFileReadException {

        final ReaderContext ctx = ReaderContext.from(file);

        ReadStages.INIT.read(ctx);
        ReadStages.METADATA.read(ctx);
        ReadStages.BLOCK_ENTITIES.read(ctx);
        ReadStages.BLOCK_STATES.read(ctx);
        ReadStages.BIOMES.read(ctx);
        ReadStages.ENTITIES.read(ctx);

        SpongeSchematic schem = ctx.builder.build();

        try {
            ctx.close();
        } catch (IOException ex) {
            // Context failed to close; this should be notified
            throw new RuntimeException(ex);
        }

        return schem;
    }
}
