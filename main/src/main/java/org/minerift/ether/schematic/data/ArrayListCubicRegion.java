package org.minerift.ether.schematic.data;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ArrayListCubicRegion<T> implements CubicRegion<T> {

    private final List<T> blocks;
    private final int width, height, length;

    public ArrayListCubicRegion(int width, int height, int length) {
        this.width = width;
        this.height = height;
        this.length = length;
        this.blocks = new ArrayList<>(width * height * length);
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
    public void setBlockAt(T block, int x, int y, int z) {
        blocks.set(getIndexAt(x, y, z), block);
    }

    public static void main(String[] args) {
        int width = 3;
        int height = 4;
        int length = 5;
        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                for(int z = 0; z < length; z++) {
                    int idx = ((x * (width+1)) + y) * (height+1) + z;
                    System.out.printf("%d (%d, %d, %d)\n", idx, x, y, z);
                }
            }
        }
    }

    private int getIndexAt(int x, int y, int z) {
        int idx = ((x * (width+1)) + y) * (height+1) + z;
        Preconditions.checkElementIndex(idx, blocks.size());
        return idx;
    }

    @Override
    public T getBlockAt(int x, int y, int z) {
        return blocks.get(getIndexAt(x, y, z));
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return blocks.iterator();
    }

}
