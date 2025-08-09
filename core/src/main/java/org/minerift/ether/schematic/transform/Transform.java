package org.minerift.ether.schematic.transform;

import org.minerift.ether.math.Vec3i;
import org.minerift.ether.schematic.data.Array3DOrder;

public interface Transform {

    byte[] transform(Array3DOrder order, int width, int height, int len, byte[] src);

    default byte[] transform(Array3DOrder order, Vec3i dim, byte[] src) {
        return transform(order, dim.getX(), dim.getY(), dim.getZ(), src);
    }

}
