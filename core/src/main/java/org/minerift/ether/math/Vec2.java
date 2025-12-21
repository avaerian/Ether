package org.minerift.ether.math;

import org.minerift.ether.debug.Experimental;

public interface Vec2 {

    Vec2 copy();

    int getX();
    int getZ();

    double getXd();
    double getZd();

    int[] getXZ();
    double[] getXZd();

    default boolean isMutable() {
        return false;
    }

    @Deprecated // not necessarily deprecated, more so asking to avoid using this
    @Experimental
    interface Mutable {
        void setX(double x);
        void setZ(double z);

        void set(double x, double z);
        void set(Vec2 vec);

        void add(double x, double z);
        void add(Vec2 addend);

        void subtract(double x, double z);
        void subtract(Vec2 subtrahend);
    }

}
