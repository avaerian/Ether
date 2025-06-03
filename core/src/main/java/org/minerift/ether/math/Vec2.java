package org.minerift.ether.math;

public interface Vec2<T extends Vec2<T>> {

    T copy();

    int getX();
    int getZ();

    double getXd();
    double getZd();

}
