package org.minerift.ether.math;

import com.google.common.collect.AbstractIterator;
import org.jetbrains.annotations.Nullable;
import org.minerift.ether.util.fn.Array3DAccessor;

public class Array3DIterator<E> extends AbstractIterator<E> {

    private final E[][][] data;
    private final int width, height, length;
    private final int dataSize;
    private final Array3DAccessor<E> arrayAccessor;

    private int idx;

    public Array3DIterator(E[][][] data) {
        this(data, Array3DAccessor.xyz());
    }

    public Array3DIterator(E[][][] data, Array3DAccessor<E> arrayAccessor) {
        this.data = data;
        this.width = data.length;
        this.height = data[0].length;
        this.length = data[0][0].length;
        this.arrayAccessor = arrayAccessor;

        this.dataSize = width * height * length;
        this.idx = 0;
    }

    @Nullable
    @Override
    protected E computeNext() {

        if(idx >= dataSize) {
            return endOfData();
        }

        // Row major order (z has fastest variation)
        final int z = idx % length;
        final int y = (idx / length) % height;
        final int x = idx / (length * height);

        idx++;
        return data[x][y][z];
        //return arrayAccessor.get(data, x, y, z);
    }
}