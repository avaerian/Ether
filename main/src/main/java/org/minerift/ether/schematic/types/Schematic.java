package org.minerift.ether.schematic.types;

import com.google.common.base.Preconditions;
import org.minerift.ether.EtherPlugin;
import org.minerift.ether.schematic.SchematicFileReadException;
import org.minerift.ether.util.Result;
import org.minerift.ether.util.math.Vec3i;

import java.io.File;

public interface Schematic {

    static Result<Schematic, SchematicFileReadException> fromFile(File file) {

        Preconditions.checkNotNull(file, "File cannot be null!");

        final Result<Schematic, SchematicFileReadException> newResult = new Result<>();
        SchematicType schemType = EtherPlugin.getInstance().isUsingWorldEdit()
                ? SchematicType.WORLDEDIT
                : SchematicType.SPONGE;

        // Load schematic and return result
        Result<? extends Schematic, SchematicFileReadException> result = schemType.getReader().read(file);
        result.handle(newResult::ok, newResult::err);
        return newResult;
    }

    SchematicType getType();

    void paste(Vec3i pos, String worldName);

    int getWidth();
    int getHeight();
    int getLength();

}
