package org.minerift.ether.database.bin.depbin.writer;

import org.minerift.ether.database.DataType;
import org.minerift.ether.database.Ref;
import org.minerift.ether.util.Utils;

import static org.minerift.ether.util.Utils.getArrayElement;
import static org.minerift.ether.util.Utils.unknownIntToInt16;

public class WriteEntry<T> {
    protected final DataType<T> type;
    protected final Object data; // Ref<T> or T
    protected final Ref arraySize;

    protected WriteEntry(DataType<T> type, Ref<T> ref, Ref arraySize) {
        this.type = type;
        this.data = ref;
        this.arraySize = arraySize;
    }

    protected WriteEntry(DataType<T> type, T data, Ref arraySize) {
        this.type = type;
        this.data = data;
        this.arraySize = arraySize;
    }

    public T unwrapData() {
        return data instanceof Ref<?> ? ((Ref<T>)data).get() : (T) data;
    }

    public boolean isArray() {
        return type.isArrayType() && !type.isExtendedBinaryType();
    }

    public boolean hasDynamicArrayLength() {
        return arraySize == null && type.hasDynamicArrayLength();
    }

    public int getArrayLength() {
        if(!type.isArrayType()) {
            throw new RuntimeException("Data type is not an array of " + type.getPrimitiveType());
        }
        if(hasDynamicArrayLength()) {
            return Utils.getArrayLength(unwrapData());
        }
        return arraySize != null ? unknownIntToInt16(arraySize.get()) : type.arrayLength();
    }

    public int getByteSize() {
        if(!isArray()) {
            return type.getByteSize(unwrapData()); // TODO: resolve
        }

        int size = getArrayLength();
        int bytes = hasDynamicArrayLength() ? Integer.BYTES : 0;
        Ref element = new Ref();
        for(int i = 0; i < size; i++) {
            element.set(getArrayElement(unwrapData(), i));
            bytes += type.getByteSize(unwrapData());
        }
        return bytes;
    }
}
