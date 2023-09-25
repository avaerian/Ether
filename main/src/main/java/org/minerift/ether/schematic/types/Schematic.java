package org.minerift.ether.schematic.types;

import com.google.common.base.Preconditions;
import org.minerift.ether.Ether;
import org.minerift.ether.math.Vec3i;
import org.minerift.ether.schematic.SchematicFileReadException;
import org.minerift.ether.schematic.SchematicPasteOptions;

import java.io.File;

public interface Schematic {

    static Schematic fromFile(File file) throws SchematicFileReadException {

        Preconditions.checkNotNull(file, "File cannot be null!");

        final SchematicType schemType = Ether.isUsingWorldEdit()
                ? SchematicType.WORLDEDIT
                : SchematicType.SPONGE;

        // Read into memory
        return schemType.getReader().read(file);
    }

    SchematicType getType();

    void paste(Vec3i pos, String worldName, SchematicPasteOptions options);

    int getWidth();
    int getHeight();
    int getLength();

    Vec3i getOffset();

}
