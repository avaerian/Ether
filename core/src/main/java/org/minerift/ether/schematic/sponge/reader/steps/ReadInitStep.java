package org.minerift.ether.schematic.sponge.reader.steps;

import org.minerift.ether.schematic.sponge.reader.SchematicReaderContext;
import org.minerift.ether.schematic.SchematicFileReadException;
import org.minerift.ether.schematic.sponge.SpongeVersion;

import static org.minerift.ether.schematic.sponge.reader.SchematicNBTFields.*;

// Strategy for reading and verifying initial data for a schematic
public class ReadInitStep implements IReaderStep {

    @Override
    public void read(SchematicReaderContext ctx) throws SchematicFileReadException {

        int iVersion = ctx.root.getInt(NBT_VERSION).orElseThrow(() -> new SchematicFileReadException("Failed to read version!"));
        SpongeVersion version = switch (iVersion) {
            case 1 -> SpongeVersion.V1;
            case 2 -> SpongeVersion.V2;
            case 3 -> SpongeVersion.V3;
            default -> throw new SchematicFileReadException("Invalid version");
        };
        ctx.builder.setVersion(version);

        int width  = ctx.root.getShort(NBT_WIDTH).orElseThrow(() -> new SchematicFileReadException("Failed to read width!"));
        int height = ctx.root.getShort(NBT_HEIGHT).orElseThrow(() -> new SchematicFileReadException("Failed to read height!"));
        int length = ctx.root.getShort(NBT_LENGTH).orElseThrow(() -> new SchematicFileReadException("Failed to read length!"));

        ctx.builder.setDimensions(width, height, length);

    }
}
