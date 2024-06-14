package org.minerift.ether.util.newnbt.primitives;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class FloatIOPrimitive implements ScalarIOPrimitive<Float> {
    @Override
    public Class<Float> getPrimitiveType() {
        return float.class;
    }

    @Override
    public int getByteSize(Float obj) {
        return Float.BYTES;
    }

    @Override
    public void write(DataOutputStream os, Float scalar) throws IOException {
        writeFloat(os, scalar);
    }

    public void writeFloat(DataOutputStream os, float scalar) throws IOException {
        os.writeFloat(scalar);
    }

    @Override
    public Float read(DataInputStream is) throws IOException {
        return readFloat(is);
    }

    public float readFloat(DataInputStream is) throws IOException {
        return is.readFloat();
    }

    @Override
    public ArrayIOPrimitive<?> arrayType() {
        return null;
    }
}
