package org.minerift.ether.util.newnbt.primitives;

import java.io.DataInputStream;
import java.io.IOException;

public interface ArrayIOPrimitive<T> extends IOPrimitive<T> {

    @Override
    default boolean isArrayType() {
        return true;
    }

    T read(DataInputStream is, int length) throws IOException;
    ScalarIOPrimitive<?> scalarType();

}
