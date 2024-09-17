package org.minerift.ether.util.newnbt.io.primitives;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class StringIOPrimitive implements ScalarIOPrimitive<String> {
    @Override
    public Class<String> getPrimitiveType() {
        return String.class;
    }

    @Override
    public int getByteSize(String obj) {
        return obj.getBytes(StandardCharsets.UTF_8).length;
    }

    @Override
    public void write(DataOutputStream os, String scalar) throws IOException {
        byte[] bytes = scalar.getBytes(StandardCharsets.UTF_8);
        short strLen = (short) bytes.length;
        SHORT.writeShort(os, strLen);
        BYTE_ARRAY.write(os, bytes);
    }

    @Override
    public int getByteSize() {
        throw new UnsupportedOperationException("Cannot get byte size for dynamically sized type String!");
    }

    @Override
    public String read(DataInputStream is) throws IOException {
        short byteCount = SHORT.read(is);
        byte[] bytes = BYTE_ARRAY.read(is, byteCount);
        String str = new String(bytes, StandardCharsets.UTF_8);
        return str;
    }

    @Override
    public ArrayIOPrimitive<String[]> arrayType() {
        throw new UnsupportedOperationException("No array type exists for StringIOPrimitive!");
    }
}
