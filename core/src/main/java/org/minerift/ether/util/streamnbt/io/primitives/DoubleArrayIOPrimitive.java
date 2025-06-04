package org.minerift.ether.util.streamnbt.io.primitives;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class DoubleArrayIOPrimitive implements ArrayIOPrimitive<double[]> {
    @Override
    public double[] read(DataInputStream is, int length) throws IOException {
        double[] doubles = new double[length];
        for(int i = 0; i < length; i++) {
            doubles[i] = scalarType().readDouble(is);
        }
        return doubles;
    }

    @Override
    public DoubleIOPrimitive scalarType() {
        return IOPrimitive.DOUBLE;
    }

    @Override
    public Class<double[]> getPrimitiveType() {
        return double[].class;
    }

    @Override
    public int getByteSize(double[] obj) {
        return scalarType().getByteSize() * obj.length;
    }

    @Override
    public void write(DataOutputStream os, double[] array) throws IOException {
        for(double value : array) {
            scalarType().writeDouble(os, value);
        }
    }
}
