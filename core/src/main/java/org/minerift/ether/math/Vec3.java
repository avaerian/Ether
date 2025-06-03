package org.minerift.ether.math;

public interface Vec3<T extends Vec3<T>> {

    T copy();

    int getX();
    int getY();
    int getZ();

    double getXd();
    double getYd();
    double getZd();

    Vec3d asVec3d();
    Vec3i asVec3i();

}
