package org.minerift.ether.util.newnbt.io.primitives;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ShortIOPrimitive implements ScalarIOPrimitive<Short> {
    @Override
    public Class<Short> getPrimitiveType() {
        return short.class;
    }

    @Override
    public int getByteSize(Short obj) {
        return Short.BYTES;
    }

    @Override
    public void write(DataOutputStream os, Short scalar) throws IOException {
        writeShort(os, scalar);
    }

    public void writeShort(DataOutputStream os, short scalar) throws IOException {
        os.writeShort(scalar);
    }

    @Override
    public Short read(DataInputStream is) throws IOException {
        return readShort(is);
    }

    public short readShort(DataInputStream is) throws IOException {
        return is.readShort();
    }

    @Override
    public ShortArrayIOPrimitive arrayType() {
        return IOPrimitive.SHORT_ARRAY;
    }
}
