package org.minerift.ether.schematic.pasters;

import org.minerift.ether.schematic.SchematicPasteOptions;
import org.minerift.ether.schematic.types.Schematic;
import org.minerift.ether.math.Vec3i;

public interface ISchematicPaster<S extends Schematic> {
    void paste(S schem, Vec3i pos, String worldName, SchematicPasteOptions options);
}
