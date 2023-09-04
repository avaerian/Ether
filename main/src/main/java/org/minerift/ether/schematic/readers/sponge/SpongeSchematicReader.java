package org.minerift.ether.schematic.readers.sponge;

import org.minerift.ether.schematic.SchematicFileReadException;
import org.minerift.ether.schematic.readers.ISchematicReader;
import org.minerift.ether.schematic.types.SpongeSchematic;
import org.minerift.ether.util.Result;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

public class SpongeSchematicReader implements ISchematicReader<SpongeSchematic> {

    @Override
    public Result<SpongeSchematic, SchematicFileReadException> read(File file) {

        AtomicReference<Result<SpongeSchematic, SchematicFileReadException>> result = new AtomicReference<>(new Result<>());
        ReaderContext.from(file).handle((ctx) -> {
            boolean success = true;
            try {
                ReadStages.INIT.read(ctx);
                ReadStages.METADATA.read(ctx);
                ReadStages.BLOCK_ENTITIES.read(ctx);
                ReadStages.BLOCK_STATES.read(ctx);
                ReadStages.BIOMES.read(ctx);
                ReadStages.ENTITIES.read(ctx);
            } catch (SchematicFileReadException e) {
                // Delegate exception to result
                result.get().err(e);
                success = false;
            }

            // Set result to new SpongeSchematic
            if(success) {
                result.get().ok(ctx.builder.build());
            }

            try {
                ctx.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        },
        // Handle context error
        (ex) -> result.get().err(ex));

        return result.get();
    }
}
