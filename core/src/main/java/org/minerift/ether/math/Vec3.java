package org.minerift.ether.math;

import java.util.function.IntUnaryOperator;

public interface Vec3 {

    Vec3 copy();

    int getX();
    int getY();
    int getZ();

    double getXd();
    double getYd();
    double getZd();

    int[] getXYZ();
    double[] getXYZd();

    Vec3d asVec3d();
    Vec3i asVec3i();

    default boolean isMutable() {
        return false;
    }

    // Immutable operations; will copy
    //Vec3 transform(IntUnaryOperator x, IntUnaryOperator y, IntUnaryOperator z);

}
