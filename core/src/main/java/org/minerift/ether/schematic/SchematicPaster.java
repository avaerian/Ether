package org.minerift.ether.schematic;

import org.minerift.ether.math.Vec3i;

public interface SchematicPaster<S extends Schematic> {
    void paste(S schem, Vec3i pos, String worldName, SchematicPasteOptions options);
}
