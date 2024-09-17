package org.minerift.ether.util.newnbt.io.primitives;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class IntArrayIOPrimitive implements ArrayIOPrimitive<int[]> {
    @Override
    public int[] read(DataInputStream is, int length) throws IOException {
        int[] ints = new int[length];
        for(int i = 0; i < length; i++) {
            ints[i] = scalarType().readInt(is);
        }
        return ints;
    }

    @Override
    public IntIOPrimitive scalarType() {
        return INT;
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
