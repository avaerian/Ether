package org.minerift.ether.util.iterator;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.Iterator;

public class ByteBufferIterator extends NIOBufferIterator<ByteBuffer, Byte> {

    public static Iterable<Byte> iterateOver(ByteBuffer buffer) {
        return new Iterable<>() {
            @NotNull
            @Override
            public Iterator<Byte> iterator() {
                return new ByteBufferIterator(buffer);
            }
        };
    }

    public static Iterable<Byte> iterateOver(ByteBuffer buffer, byte invalidSym) {
        return new Iterable<>() {
            @NotNull
            @Override
            public Iterator<Byte> iterator() {
                return new ByteBufferIterator(buffer, invalidSym);
            }
        };
    }

    public ByteBufferIterator(ByteBuffer buffer, byte invalidSym) {
        super(buffer.asReadOnlyBuffer(), ByteBuffer::get, invalidSym);
    }

    public ByteBufferIterator(ByteBuffer buffer) {
        this(buffer, (byte) -1);
    }
}
