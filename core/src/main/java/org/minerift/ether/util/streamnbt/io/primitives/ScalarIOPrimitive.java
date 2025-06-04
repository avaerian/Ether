package org.minerift.ether.util.streamnbt.io.primitives;

public interface ScalarIOPrimitive<T> extends IOPrimitive<T> {

    default int getByteSize() {
        return getByteSize(null);
    }

    @Override
    default boolean isArrayType() {
        return false;
    }

    ArrayIOPrimitive<?> arrayType();

}
