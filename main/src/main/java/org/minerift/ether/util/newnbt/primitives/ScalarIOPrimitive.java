package org.minerift.ether.util.newnbt.primitives;

import java.io.DataInputStream;
import java.io.IOException;

public interface ScalarIOPrimitive<T> extends IOPrimitive<T> {

    default int getByteSize() {
        return getByteSize(null);
    }

    @Override
    default boolean isArrayType() {
        return false;
    }

    T read(DataInputStream is) throws IOException;
    ArrayIOPrimitive<?> arrayType();

}
