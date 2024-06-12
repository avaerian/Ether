package org.minerift.ether.math;

import com.google.common.collect.AbstractIterator;
import org.jetbrains.annotations.Nullable;

@Deprecated
public class DeprecatedArray3DIterator<E> extends AbstractIterator<E> {

    private final E[][][] data;
    private final int width, height, length;

    private int xIdx, yIdx, zIdx = 0;

    public DeprecatedArray3DIterator(E[][][] data) {
        this.data = data;
        this.width = data.length;
        this.height = data[0].length;
        this.length = data[0][0].length;
    }

    @Nullable
    @Override
    protected E computeNext() {
        if(xIdx == width) { // once out of bounds, close
            return endOfData();
        }

        //System.out.println(String.format("%d: (%d, %d, %d)", count, xIdx, yIdx, zIdx)); // debug
        E current = data[xIdx][yIdx][zIdx];

        // Gather info about current state before updating
        boolean endOfZ = zIdx == length - 1;
        boolean endOfY = yIdx == height - 1;

        // Update z first for fastest variation
        if(endOfZ) {
            yIdx++;
            zIdx = 0;

            if(endOfY) {
                xIdx++;
                yIdx = 0;
            }
        }

        // Increment for next element in 3d array
        if(!endOfZ) {
            zIdx++;
        }

        return current;
    }
}
