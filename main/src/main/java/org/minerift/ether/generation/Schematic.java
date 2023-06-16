package org.minerift.ether.generation;

import com.google.common.base.Preconditions;
import org.bukkit.Location;
import org.jnbt.CompoundTag;
import org.jnbt.NBTInputStream;
import org.jnbt.Tag;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.minerift.ether.generation.Schematic.SchematicFormat.*;

// A structure that can be placed in the world
// Both native and WorldEdit API implementations will be provided
public class Schematic {

    private short width, height, length;

    private byte[] blocks, data;

    private List<Tag> entities;
    private List<Tag> tileEntities;

    public static Schematic fromFile(File file) {
        Preconditions.checkNotNull(file);
        Preconditions.checkArgument(file.length() != 0);

        // TODO
        Schematic schem = null;
        return schem;
    }

    private Schematic(File file) {

        try(FileInputStream fileInputStream = new FileInputStream(file); NBTInputStream nbt = new NBTInputStream(fileInputStream)) {

            CompoundTag head = (CompoundTag) nbt.readTag();
            Map<String, Tag> tags = head.getValue();

            // Read tags into object
            this.width = readValue(tags, WIDTH);
            this.height = readValue(tags, HEIGHT);
            this.length = readValue(tags, LENGTH);
            this.blocks = readValue(tags, BLOCKS);
            this.data = readValue(tags, DATA);
            this.entities = readValue(tags, ENTITIES);
            this.tileEntities = readValue(tags, TILE_ENTITIES);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Index maths found here: https://minecraft.fandom.com/wiki/Schematic_file_format
    private int getIndexForBlocksArray(int x, int y, int z) {
        return (y * length + z) * width + x;
    }

    private int getIndexForBlocksArray(Location location) {
        return getIndexForBlocksArray(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    private <T> T readValue(Map<String, Tag> tags, String tag) {
        return (T) tags.get(tag).getValue();
    }

    public static class SchematicFormat {
        public static final String WIDTH = "Width";
        public static final String HEIGHT = "Height";
        public static final String LENGTH = "Length";
        public static final String BLOCKS = "Blocks";
        public static final String DATA = "Data";
        public static final String ENTITIES = "Entities";
        public static final String TILE_ENTITIES = "TileEntities";
        public static final String PALETTE = "Palette";
        public static final String PALETTE_MAX = "PaletteMax";

        private SchematicFormat() {}

    }
}
