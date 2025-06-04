package org.minerift.ether.util.streamnbt.io.primitives;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class FloatArrayIOPrimitive implements ArrayIOPrimitive<float[]> {
    @Override
    public float[] read(DataInputStream is, int length) throws IOException {
        float[] floats = new float[length];
        for(int i = 0; i < length; i++) {
            floats[i] = scalarType().readFloat(is);
        }
        return floats;
    }

    @Override
    public FloatIOPrimitive scalarType() {
        return IOPrimitive.FLOAT;
    }

    @Override
    public Class<float[]> getPrimitiveType() {
        return float[].class;
    }

    @Override
    public int getByteSize(float[] obj) {
        return scalarType().getByteSize() * obj.length;
    }

    @Override
    public void write(DataOutputStream os, float[] array) throws IOException {
        for(float value : array) {
            scalarType().writeFloat(os, value);
        }
    }
}
