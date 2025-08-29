package org.minerift.ether.schematic;

import com.google.common.base.Preconditions;
import org.minerift.ether.Ether;
import org.minerift.ether.math.Vec3i;
import org.minerift.ether.schematic.transform.Transforms;

import java.io.File;

public interface Schematic {

    static Schematic fromFile(SchematicType type, File file) throws SchematicFileReadException {
        Preconditions.checkNotNull(file, "File cannot be null!");
        return type.getReader().read(file);
    }

    static Schematic fromFile(File file) throws SchematicFileReadException {
        final SchematicType type = Ether.isUsingWorldEdit()
                ? SchematicType.WORLDEDIT
                : SchematicType.SPONGE;
        return fromFile(type, file);
    }

    SchematicType getType();

    void paste(Vec3i pos, String worldName, SchematicPasteOptions options);

    int getWidth();
    int getHeight();
    int getLength();

    Vec3i getDimensions();
    Vec3i getOffset();

    Schematic transform(Transforms ts);
    Schematic transformMut(Transforms ts);

}
