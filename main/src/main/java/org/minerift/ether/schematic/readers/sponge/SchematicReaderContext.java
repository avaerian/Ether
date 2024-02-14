package org.minerift.ether.schematic.readers.sponge;

import com.google.common.base.Preconditions;
import org.minerift.ether.schematic.SchematicFileReadException;
import org.minerift.ether.schematic.types.SpongeSchematic;
import org.minerift.ether.util.nbt.NBTInputStream;
import org.minerift.ether.util.nbt.NBTSectionView;
import org.minerift.ether.util.nbt.tags.CompoundTag;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class SchematicReaderContext {

    private final NBTInputStream nbt;
    private final CompoundTag head;

    public final NBTSectionView rootView;
    public final SpongeSchematic.Builder builder;

    public static SchematicReaderContext from(File file) throws SchematicFileReadException {
        try {
            return new SchematicReaderContext(file);
        } catch (IOException ex) {
            throw (SchematicFileReadException) ex;
        }
    }

    private SchematicReaderContext(File file) throws IOException {

        Preconditions.checkNotNull(file, "File cannot be null!");

        // Attempt to read file
        this.nbt = new NBTInputStream(new FileInputStream(file));
        this.head = (CompoundTag) nbt.readTag();
        this.rootView = new NBTSectionView(head);
        this.builder = SpongeSchematic.builder();
    }

    public void close() throws IOException {
        nbt.close();
    }
}
