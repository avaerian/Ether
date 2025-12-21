package org.minerift.ether.schematic;

import io.netty.buffer.ByteBuf;
import org.minerift.ether.util.UnreachableException;
import org.minerift.ether.util.nbt.Compression;
import org.minerift.ether.util.nbt.NbtWriter;
import org.minerift.ether.util.nbt.tags.container.CompoundTag;

import java.io.File;
import java.io.IOException;

public interface SchematicCodec<S extends Schematic> {

    // writer flags
    int NO_FLAGS = 0;
    int WRITE_BIOMES_ALWAYS = 1;
    int WRITE_ENTITIES_ALWAYS = 2;
    int EXCLUDE_BIOMES = 4;
    int EXCLUDE_ENTITIES = 8;

    S read(File file) throws SchematicReadException;
    S read(CompoundTag tag) throws SchematicReadException;
    S read(ByteBuf buf, Compression compress) throws SchematicReadException;

    default S read(ByteBuf buf) throws SchematicReadException {
        return read(buf, Compression.NONE);
    }

    default int write(S schem, ByteBuf buf, Compression compress, int flags) throws IOException {
        CompoundTag schemTag = schem.writeAsNbt(flags);
        NbtWriter writer = NbtWriter.from(buf);
        writer.writeTag(schemTag);
        writer.compress(compress);
        return writer.buf.readableBytes();
    }

    default int write(S schem, ByteBuf buf, int flags) {
        try {
            return write(schem, buf, Compression.NONE, flags);
        } catch (IOException e) {
            throw new UnreachableException("No compression for NbtWriter buf");
        }
    }

    default int write(S schem, ByteBuf buf) {
        try {
            return write(schem, buf, Compression.NONE, NO_FLAGS);
        } catch (IOException e) {
            throw new UnreachableException("No compression for NbtWriter buf", e);
        }
    }

    default int write(S schem, File file, Compression compress, int flags) throws IOException {
        CompoundTag schemTag = schem.writeAsNbt(flags);
        NbtWriter writer = NbtWriter.from();
        writer.writeTag(schemTag);
        writer.compress(compress);
        return writer.dump(file);
    }

    default int write(S schem, File file, int flags) throws IOException {
        return write(schem, file, Compression.NONE, flags);
    }

    default int write(S schem, File file) throws IOException {
        return write(schem, file, Compression.NONE, NO_FLAGS);
    }

    CompoundTag writeAsNbt(S schem, int flags);

}
