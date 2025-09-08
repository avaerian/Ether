package org.minerift.ether.schematic.sponge.reader.steps;

import org.minerift.ether.schematic.sponge.reader.SchematicReaderContext;
import org.minerift.ether.schematic.SchematicFileReadException;
import org.minerift.ether.schematic.sponge.SpongeVersion;

import static org.minerift.ether.schematic.sponge.reader.SchematicNBTFields.*;

// Strategy for reading and verifying initial data for a schematic
public class ReadInitStep implements IReaderStep {

    @Override
    public void read(SchematicReaderContext ctx) throws SchematicFileReadException {
        int _version = ctx.root.getInt(NBT_VERSION,
                        (e) -> new SchematicFileReadException("Failed to read version", e));

        SpongeVersion version = switch (_version) {
            case 1 -> SpongeVersion.V1;
            case 2 -> SpongeVersion.V2;
            case 3 -> SpongeVersion.V3;
            default -> throw new SchematicFileReadException("Invalid version");
        };
        ctx.builder.setVersion(version);

        short w = ctx.root.getShort(NBT_WIDTH, (e) -> new SchematicFileReadException("Failed to read width", e));
        short h = ctx.root.getShort(NBT_HEIGHT, (e) -> new SchematicFileReadException("Failed to read height", e));
        short l = ctx.root.getShort(NBT_LENGTH, (e) -> new SchematicFileReadException("Failed to read length", e));
        ctx.builder.setDimensions(w, h, l);
    }
}
