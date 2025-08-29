package org.minerift.ether.schematic.transform;

import org.minerift.ether.math.Vec3d;
import org.minerift.ether.math.Vec3i;
import org.minerift.ether.schematic.data.Array3DOrder;

public interface Transform {

    // TODO: swap so overload is this method while Vec3i can be implemented
    Result<byte[]> transform(Array3DOrder order, int width, int height, int len, byte[] src);

    default Result<byte[]> transform(Array3DOrder order, Vec3i dim, byte[] src) {
        return transform(order, dim.getX(), dim.getY(), dim.getZ(), src);
    }

    // TODO: implement static function for transforming coordinates
    Result<Vec3i> transformVec(Vec3i dim, Vec3i vec);
    default Result<Vec3i> transformVec(int dimX, int dimY, int dimZ, Vec3i vec) {
        return transformVec(new Vec3i(dimX, dimY, dimZ), vec);
    }

    Result<Vec3d> transformVec(Vec3i dim, Vec3d vec);
    default Result<Vec3d> transformVec(int dimX, int dimY, int dimZ, Vec3d vec) {
        return transformVec(new Vec3i(dimX, dimY, dimZ), vec);
    }

    class Result<T> {
        //public final byte[] buf;
        public final T out;
        public final Vec3i dim;

        public Result(Vec3i dim, /*byte[] buf*/ T out) {
            this.dim = dim;
            this.out = out;
        }
    }

}
