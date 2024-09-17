package org.minerift.ether.util.newnbt.io.primitives;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class IntIOPrimitive implements ScalarIOPrimitive<Integer> {
    @Override
    public Class<Integer> getPrimitiveType() {
        return int.class;
    }

    @Override
    public int getByteSize(Integer obj) {
        return Integer.BYTES;
    }

    @Override
    public void write(DataOutputStream os, Integer scalar) throws IOException {
        writeInt(os, scalar);
    }

    public void writeInt(DataOutputStream os, int scalar) throws IOException {
        os.writeInt(scalar);
    }

    @Override
    public Integer read(DataInputStream is) throws IOException {
        return readInt(is);
    }

    public int readInt(DataInputStream is) throws IOException {
        return is.readInt();
    }

    @Override
    public IntArrayIOPrimitive arrayType() {
        return INT_ARRAY;
    }
}
