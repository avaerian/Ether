package org.minerift.ether.math;

public interface Vec3 {

    Vec3 copy();

    int getX();
    int getY();
    int getZ();

    double getXd();
    double getYd();
    double getZd();

    Vec3d asVec3d();
    Vec3i asVec3i();

}
