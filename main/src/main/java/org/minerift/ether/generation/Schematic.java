package org.minerift.ether.generation;

import com.google.common.base.Preconditions;

import java.io.File;

// A structure that can be placed in the world
// Both Native and WorldEdit API implementations will be provided
public class Schematic {

    private short width, height, length;

    private byte[] blocks;

    public static void fromFile(File file) {
        Preconditions.checkNotNull(file);
        Preconditions.checkArgument(file.length() != 0);

        //NB
    }

}
