package org.minerift.ether.schematic.pasters;

import org.minerift.ether.schematic.types.Schematic;
import org.minerift.ether.util.math.Vec3i;

public interface ISchematicPaster<S extends Schematic> {
    void paste(S schem, Vec3i pos, String worldName);
}
