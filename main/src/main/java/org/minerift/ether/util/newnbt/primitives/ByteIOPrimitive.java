package org.minerift.ether.util.newnbt.primitives;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ByteIOPrimitive implements ScalarIOPrimitive<Byte> {
    @Override
    public Class<Byte> getPrimitiveType() {
        return byte.class;
    }

    @Override
    public int getByteSize(Byte obj) {
        return Byte.BYTES;
    }

    @Override
    public Byte read(DataInputStream is) throws IOException {
        return readByte(is);
    }

    public byte readByte(DataInputStream is) throws IOException {
        return is.readByte();
    }

    @Override
    public void write(DataOutputStream os, Byte scalar) throws IOException {
        writeByte(os, scalar);
    }

    public void writeByte(DataOutputStream os, byte scalar) throws IOException {
        os.writeByte(scalar);
    }

    @Override
    public ByteArrayIOPrimitive arrayType() {
        return IOPrimitive.BYTE_ARRAY;
    }
}
