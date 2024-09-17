package org.minerift.ether.util.newnbt.io.primitives;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ShortArrayIOPrimitive implements ArrayIOPrimitive<short[]> {
    @Override
    public short[] read(DataInputStream is, int length) throws IOException {
        short[] shorts = new short[length];
        for(int i = 0; i < length; i++) {
            shorts[i] = scalarType().readShort(is);
        }
        return shorts;
    }

    @Override
    public ShortIOPrimitive scalarType() {
        return IOPrimitive.SHORT;
    }

    @Override
    public Class<short[]> getPrimitiveType() {
        return short[].class;
    }

    @Override
    public int getByteSize(short[] obj) {
        return scalarType().getByteSize() * obj.length;
    }

    @Override
    public void write(DataOutputStream os, short[] array) throws IOException {
        for (short value : array) {
            scalarType().writeShort(os, value);
        }
    }
}
