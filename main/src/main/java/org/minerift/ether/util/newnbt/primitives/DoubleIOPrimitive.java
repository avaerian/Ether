package org.minerift.ether.util.newnbt.primitives;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class DoubleIOPrimitive implements ScalarIOPrimitive<Double> {
    @Override
    public Class<Double> getPrimitiveType() {
        return double.class;
    }

    @Override
    public int getByteSize(Double obj) {
        return Double.BYTES;
    }

    @Override
    public void write(DataOutputStream os, Double scalar) throws IOException {
        writeDouble(os, scalar);
    }

    public void writeDouble(DataOutputStream os, double scalar) throws IOException {
        os.writeDouble(scalar);
    }

    @Override
    public Double read(DataInputStream is) throws IOException {
        return readDouble(is);
    }

    public double readDouble(DataInputStream is) throws IOException {
        return is.readDouble();
    }

    @Override
    public DoubleArrayIOPrimitive arrayType() {
        return IOPrimitive.DOUBLE_ARRAY;
    }
}
