package org.minerift.ether.util.streamnbt.io.primitives;

import java.io.DataInputStream;
import java.io.IOException;

public interface ArrayIOPrimitive<T> extends IOPrimitive<T> {

    @Override
    default boolean isArrayType() {
        return true;
    }

    @Override
    default T read(DataInputStream is) throws IOException {
        int length = INT.read(is);
        return read(is, length);
    }

    T read(DataInputStream is, int length) throws IOException;
    ScalarIOPrimitive<?> scalarType();

}
