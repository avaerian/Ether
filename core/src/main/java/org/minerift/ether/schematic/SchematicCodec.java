package org.minerift.ether.schematic;

import io.netty.buffer.ByteBuf;
import org.minerift.ether.util.nbt.tags.container.CompoundTag;

import java.io.File;

public interface SchematicCodec<S extends Schematic> {

    // writer flags
    public static final int NO_FLAGS = 0;
    public static final int WRITE_BIOMES_ALWAYS = 1;
    public static final int WRITE_ENTITIES_ALWAYS = 2;
    public static final int EXCLUDE_BIOMES = 4;
    public static final int EXCLUDE_ENTITIES = 8;

    S read(File file) throws SchematicReadException;
    S read(ByteBuf buf) throws SchematicReadException;
    S read(CompoundTag tag) throws SchematicReadException;

    int write(S schem, ByteBuf buf, int flags);
    int write(S schem, File file, int flags);
    default int write(S schem, ByteBuf buf) {
        return write(schem, buf, NO_FLAGS);
    }
    default int write(S schem, File file) {
        return write(schem, file, NO_FLAGS);
    }

    CompoundTag writeAsNbt(S schem, int flags);

}
