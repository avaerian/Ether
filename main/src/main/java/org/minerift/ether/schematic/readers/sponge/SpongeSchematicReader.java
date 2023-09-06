package org.minerift.ether.schematic.readers.sponge;

import org.minerift.ether.schematic.SchematicFileReadException;
import org.minerift.ether.schematic.readers.ISchematicReader;
import org.minerift.ether.schematic.types.SpongeSchematic;
import org.minerift.ether.util.Result;

import java.io.File;
import java.io.IOException;

public class SpongeSchematicReader implements ISchematicReader<SpongeSchematic> {

    @Override
    public Result<SpongeSchematic, SchematicFileReadException> read(File file) {

        Result<SpongeSchematic, SchematicFileReadException> result = new Result<>();
        ReaderContext.from(file).handle((ctx) -> {
            try {
                ReadStages.INIT.read(ctx);
                ReadStages.METADATA.read(ctx);
                ReadStages.BLOCK_ENTITIES.read(ctx);
                ReadStages.BLOCK_STATES.read(ctx);
                ReadStages.BIOMES.read(ctx);
                ReadStages.ENTITIES.read(ctx);

                result.ok(ctx.builder.build());

                ctx.close();
            } catch (SchematicFileReadException ex) {
                // Delegate exception to result
                result.err(ex);
            } catch (IOException ex) {
                // Failed to close context; this should be notified
                throw new RuntimeException(ex);
            }
        },
        // Delegate context error to result
        result::err);

        return result;
    }
}
