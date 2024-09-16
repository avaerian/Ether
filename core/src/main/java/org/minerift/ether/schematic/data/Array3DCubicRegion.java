package org.minerift.ether.schematic.data;

import org.jetbrains.annotations.NotNull;
import org.minerift.ether.math.Array3DIterator;
import org.minerift.ether.util.Array3DAccessor;

import java.util.Iterator;

public class Array3DCubicRegion implements CubicRegion<String> {

    private final int width, height, length;
    private final String[][][] data;
    private final Array3DAccessor<String> arrayAccessor;

    public Array3DCubicRegion(int width, int height, int length) {
        this(width, height, length, Array3DAccessor.xyz());
    }

    public Array3DCubicRegion(int width, int height, int length, Array3DAccessor<String> arrayAccessor) {
        this.width = width;
        this.height = height;
        this.length = length;
        this.data = new String[width][height][length];
        this.arrayAccessor = arrayAccessor;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public int getLength() {
        return length;
    }

    @Override
    public void setBlockAt(String str, int x, int y, int z) {
        arrayAccessor.set(data, str, x, y, z);
    }

    @Override
    public String getBlockAt(int x, int y, int z) {
        return arrayAccessor.get(data, x, y, z);
    }

    @NotNull
    @Override
    public Iterator<String> iterator() {
        return new Array3DIterator<>(data, arrayAccessor);
    }

}
