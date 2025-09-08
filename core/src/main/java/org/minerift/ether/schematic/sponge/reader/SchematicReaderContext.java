package org.minerift.ether.schematic.sponge.reader;

import com.google.common.base.Preconditions;
import org.minerift.ether.schematic.SchematicFileReadException;
import org.minerift.ether.schematic.sponge.SpongeSchematic;
import org.minerift.ether.util.nbt.Compression;
import org.minerift.ether.util.nbt.NbtReadException;
import org.minerift.ether.util.nbt.NbtReader;
import org.minerift.ether.util.nbt.tags.container.CompoundTag;

import java.io.File;
import java.io.IOException;

public class SchematicReaderContext {

    public final CompoundTag root;
    public final SpongeSchematic.Builder builder;

    public static SchematicReaderContext from(File file) throws SchematicFileReadException {
        try {
            return new SchematicReaderContext(file);
        } catch (NbtReadException | IOException ex) {
            throw new SchematicFileReadException(ex);
        }
    }

    private SchematicReaderContext(File file) throws NbtReadException, IOException {
        Preconditions.checkNotNull(file, "File cannot be null");
        NbtReader reader = NbtReader.from(file, Compression.GZIP); // TODO: review compression
        this.root = (CompoundTag) reader.readNextTag();
        this.builder = SpongeSchematic.builder();
    }
}
