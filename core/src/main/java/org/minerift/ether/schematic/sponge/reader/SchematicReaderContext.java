package org.minerift.ether.schematic.sponge.reader;

import com.google.common.base.Preconditions;
import org.minerift.ether.schematic.SchematicReadException;
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

    public static SchematicReaderContext from(File file) throws SchematicReadException {
        try {
            return new SchematicReaderContext(file);
        } catch (NbtReadException | IOException ex) {
            throw new SchematicReadException(ex);
        }
    }

    public SchematicReaderContext(CompoundTag root, SpongeSchematic.Builder builder) {
        this.root = root;
        this.builder = builder;
    }

    @Deprecated
    private SchematicReaderContext(File file) throws NbtReadException, IOException {
        Preconditions.checkNotNull(file, "File cannot be null");
        NbtReader nbt = NbtReader.from(file, Compression.GZIP);
        this.root = (CompoundTag) nbt.readNextTag();
        this.builder = SpongeSchematic.builder();
    }
}
