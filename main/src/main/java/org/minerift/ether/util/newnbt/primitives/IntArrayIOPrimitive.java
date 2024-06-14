package org.minerift.ether.util.newnbt.primitives;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class IntArrayIOPrimitive implements ArrayIOPrimitive<int[]> {
    @Override
    public int[] read(DataInputStream is, int length) throws IOException {
        return new int[0];
    }

    @Override
    public IntIOPrimitive scalarType() {
        return IOPrimitive.INT;
    }

    @Override
    public Class<int[]> getPrimitiveType() {
        return int[].class;
    }

    @Override
    public int getByteSize(int[] obj) {
        return scalarType().getByteSize() * obj.length;
    }

    @Override
    public void write(DataOutputStream os, int[] scalar) throws IOException {
        for(int value : scalar) {
            scalarType().writeInt(os, value);
        }
    }
}
