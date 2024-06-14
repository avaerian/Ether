package org.minerift.ether.util.newnbt.primitives;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ByteArrayIOPrimitive implements ArrayIOPrimitive<byte[]> {

    @Override
    public Class<byte[]> getPrimitiveType() {
        return byte[].class;
    }

    @Override
    public int getByteSize(byte[] obj) {
        return scalarType().getByteSize() * obj.length;
    }

    @Override
    public byte[] read(DataInputStream is, int length) throws IOException {
        byte[] buf = new byte[length];
        is.readFully(buf);
        return buf;
    }

    @Override
    public void write(DataOutputStream os, byte[] array) throws IOException {
        os.write(array);
    }

    @Override
    public ByteIOPrimitive scalarType() {
        return IOPrimitive.BYTE;
    }
}
