package org.minerift.ether.schematic.readers.sponge;

import com.google.common.base.Preconditions;
import org.minerift.ether.schematic.SchematicFileReadException;
import org.minerift.ether.schematic.types.SpongeSchematic;
import org.minerift.ether.util.Result;
import org.minerift.ether.util.nbt.NBTInputStream;
import org.minerift.ether.util.nbt.NBTSectionView;
import org.minerift.ether.util.nbt.tags.CompoundTag;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class ReaderContext {

    private final NBTInputStream nbt;
    private final CompoundTag head;

    public final NBTSectionView rootView;
    public final SpongeSchematic.Builder builder;

    public static Result<ReaderContext, SchematicFileReadException> from(File file) {
        final Result<ReaderContext, SchematicFileReadException> result = new Result<>();
        try {
            result.ok(new ReaderContext(file));
        } catch (IOException ex) {
            result.err((SchematicFileReadException) ex);
        }
        return result;
    }

    private ReaderContext(File file) throws IOException {

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
