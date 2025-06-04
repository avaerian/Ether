package org.minerift.ether.util.streamnbt.io.primitives;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public interface IOPrimitive<T> {

    public static final ByteIOPrimitive BYTE = new ByteIOPrimitive();
    public static final ByteArrayIOPrimitive BYTE_ARRAY = new ByteArrayIOPrimitive();
    public static final ShortIOPrimitive SHORT = new ShortIOPrimitive();
    public static final ShortArrayIOPrimitive SHORT_ARRAY = new ShortArrayIOPrimitive();
    public static final IntIOPrimitive INT = new IntIOPrimitive();
    public static final IntArrayIOPrimitive INT_ARRAY = new IntArrayIOPrimitive();
    public static final LongIOPrimitive LONG = new LongIOPrimitive();
    public static final LongArrayIOPrimitive LONG_ARRAY = new LongArrayIOPrimitive();
    public static final FloatIOPrimitive FLOAT = new FloatIOPrimitive();
    public static final FloatArrayIOPrimitive FLOAT_ARRAY = new FloatArrayIOPrimitive();
    public static final DoubleIOPrimitive DOUBLE = new DoubleIOPrimitive();
    public static final DoubleArrayIOPrimitive DOUBLE_ARRAY = new DoubleArrayIOPrimitive();
    public static final StringIOPrimitive STRING = new StringIOPrimitive();


    default boolean isScalarType() {
        return !isArrayType();
    }
    boolean isArrayType();
    Class<T> getPrimitiveType();
    int getByteSize(T obj);
    void write(DataOutputStream os, T scalar) throws IOException;
    T read(DataInputStream is) throws IOException;
}