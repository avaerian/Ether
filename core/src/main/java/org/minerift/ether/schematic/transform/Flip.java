package org.minerift.ether.schematic.transform;

import org.minerift.ether.math.Vec3d;
import org.minerift.ether.math.Vec3i;
import org.minerift.ether.schematic.data.Array3DOrder;

@Deprecated
// TODO
public class Flip implements Transform {

    @Override
    public Result<byte[]> transform(Array3DOrder order, int width, int height, int len, byte[] src) {
        return null;
    }

    @Override
    public Result<Vec3i> transformVec(Vec3i dim, Vec3i vec) {
        return null;
    }

    @Override
    public Result<Vec3d> transformVec(Vec3i dim, Vec3d vec) {
        return null;
    }
}
