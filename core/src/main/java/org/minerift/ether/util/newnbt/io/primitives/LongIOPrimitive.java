package org.minerift.ether.util.newnbt.io.primitives;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class LongIOPrimitive implements ScalarIOPrimitive<Long> {
    @Override
    public Class<Long> getPrimitiveType() {
        return long.class;
    }

    @Override
    public int getByteSize(Long obj) {
        return Long.BYTES;
    }

    @Override
    public void write(DataOutputStream os, Long scalar) throws IOException {
        writeLong(os, scalar);
    }

    public void writeLong(DataOutputStream os, long scalar) throws IOException {
        os.writeLong(scalar);
    }

    @Override
    public Long read(DataInputStream is) throws IOException {
        return readLong(is);
    }

    public long readLong(DataInputStream is) throws IOException {
        return is.readLong();
    }

    @Override
    public LongArrayIOPrimitive arrayType() {
        return LONG_ARRAY;
    }
}
