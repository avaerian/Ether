package org.minerift.ether.schematic.readers.sponge.steps;

import org.minerift.ether.schematic.readers.sponge.SchematicReaderContext;
import org.minerift.ether.schematic.SchematicFileReadException;

import static org.minerift.ether.schematic.readers.sponge.SchematicNBTFields.*;

// Strategy for reading and verifying initial data for a schematic
public class ReadInitStep implements IReaderStep {

    @Override
    public void read(SchematicReaderContext ctx) throws SchematicFileReadException {

        ctx.builder.setVersion( ctx.rootView.getInt(NBT_VERSION).orElseThrow(() -> new SchematicFileReadException("Failed to read version!"))   );
        ctx.builder.setWidth(   ctx.rootView.getShort(NBT_WIDTH).orElseThrow(() -> new SchematicFileReadException("Failed to read width!"))     );
        ctx.builder.setHeight(  ctx.rootView.getShort(NBT_HEIGHT).orElseThrow(() -> new SchematicFileReadException("Failed to read height!"))   );
        ctx.builder.setLength(  ctx.rootView.getShort(NBT_LENGTH).orElseThrow(() -> new SchematicFileReadException("Failed to read length!"))   );

    }
}
