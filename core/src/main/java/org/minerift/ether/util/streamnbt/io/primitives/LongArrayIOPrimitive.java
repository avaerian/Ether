package org.minerift.ether.util.streamnbt.io.primitives;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class LongArrayIOPrimitive implements ArrayIOPrimitive<long[]> {
    @Override
    public long[] read(DataInputStream is, int length) throws IOException {
        long[] longs = new long[length];
        for(int i = 0; i < length; i++) {
            longs[i] = scalarType().readLong(is);
        }
        return longs;
    }

    @Override
    public LongIOPrimitive scalarType() {
        return IOPrimitive.LONG;
    }

    @Override
    public Class<long[]> getPrimitiveType() {
        return long[].class;
    }

    @Override
    public int getByteSize(long[] obj) {
        return scalarType().getByteSize() * obj.length;
    }

    @Override
    public void write(DataOutputStream os, long[] scalar) throws IOException {
        for(long value : scalar) {
            scalarType().writeLong(os, value);
        }
    }
}
