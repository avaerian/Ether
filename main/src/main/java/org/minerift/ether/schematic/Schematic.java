package org.minerift.ether.schematic;

import org.jnbt.CompoundTag;
import org.jnbt.NBTInputStream;
import org.minerift.ether.EtherPlugin;
import org.minerift.ether.schematic.readers.SpongeSchematicReader;
import org.minerift.ether.schematic.types.SpongeSchematic;
import org.minerift.ether.schematic.types.WorldEditSchematic;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public interface Schematic {

    public static final String NBT_VERSION = "Version";

    static Schematic fromFile(File file) throws IOException {

        if(EtherPlugin.getInstance().isUsingWorldEdit()) {
            return new WorldEditSchematic(file);
        }

        try(FileInputStream fis = new FileInputStream(file); NBTInputStream nbt = new NBTInputStream(fis)) {

            // Read version
            CompoundTag head = (CompoundTag) nbt.readTag();
            int version = NBTTagReader.getIntOrThrow(head, NBT_VERSION);
            /*switch (version) {
                case 1 -> ()
            }*/
        }
        return null;
    }

    int getWidth();
    int getHeight();
    int getLength();

}
