package org.minerift.ether.schematic;

import org.minerift.ether.util.nbt.tags.CompoundTag;
import org.minerift.ether.util.nbt.NBTInputStream;
import org.minerift.ether.EtherPlugin;
import org.minerift.ether.schematic.types.WorldEditSchematic;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public interface Schematic {

    static Schematic fromFile(File file) throws IOException {

        if(EtherPlugin.getInstance().isUsingWorldEdit()) {
            return new WorldEditSchematic(file);
        }


        return null;
    }

    int getWidth();
    int getHeight();
    int getLength();

}
