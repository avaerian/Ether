package org.minerift.ether.util.math;

public class Maths {

    // Index maths found here: https://minecraft.fandom.com/wiki/Schematic_file_format
    public static int posAsIndex(int x, int y, int z, int width, int length) {
        //x + z * Width + y * Width * Length
        //return (y * length + z) * width + x;
        return x + (z * width) + (y * width * length);
    }

    // TODO: unfuck this
    // https://math.stackexchange.com/questions/3758576/how-to-convert-from-a-flattened-3d-index-to-a-set-of-coordinates
    @Deprecated
    public static int[] indexAsPos(int index, int width, int length) {
        final int x = index % width;
        index /= width;
        final int y = index % length;
        final int z = index / length;
        return new int[]{x, y, z};

        //final int z = idx / (xMax * yMax);
        //idx -= (z * xMax * yMax);
        //final int y = idx / xMax;
        //final int x = idx % xMax;
    }

    // TODO: unfuck this
    @Deprecated
    public static int[] indexAsPos_New(int index, int width, int length) {

        final int z = index / (width * length);
        index -= z * width * length;
        final int y = z / width;
        final int x = index - (y * width);

        return new int[]{x, y, z};
    }



}
