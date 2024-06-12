package org.minerift.ether.schematic.data;

import org.minerift.ether.math.Vec3i;

public interface CubicRegion<BlockData> extends Iterable<BlockData> {

    int getWidth();
    int getHeight();
    int getLength();
    default Vec3i getDimensions() {
        return new Vec3i(getWidth(), getHeight(), getLength());
    }

    void setBlockAt(BlockData data, int x, int y, int z);
    default void setBlockAt(BlockData data, Vec3i pos) {
        setBlockAt(data, pos.getX(), pos.getY(), pos.getZ());
    }

    BlockData getBlockAt(int x, int y, int z);

}
