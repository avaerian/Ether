package org.minerift.ether.util.fn;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public interface ByteIterator extends Iterator<Byte> {

    static ByteIterator of(byte[] bytes) {
        return new ByteIteratorImpl(bytes);
    }

    static ByteIterator of(byte[] bytes, int pos) {
        return new ByteIteratorImpl(bytes, bytes.length, pos);
    }

    static ByteIterator of(byte[] bytes, int size, int pos) {
        return new ByteIteratorImpl(bytes, size, pos);
    }

    class ByteIteratorImpl implements ByteIterator {

        private final byte[] bytes;
        private final int size;
        private int pos;

        protected ByteIteratorImpl(byte[] bytes) {
            this(bytes, bytes.length, 0);
        }

        protected ByteIteratorImpl(byte[] bytes, int size, int pos) {
            Preconditions.checkArgument(size <= bytes.length, "Size " + size + " exceeds array size " + bytes.length);
            this.bytes = bytes;
            this.size = size;
            this.pos = pos;
        }

        @Override
        public byte nextByte() {
            return bytes[pos++];
        }

        @Override
        public boolean hasNext() {
            return pos < size;
        }
    }

    byte nextByte();

    @Deprecated
    default Byte next() {
        return nextByte();
    }

    default void remove() {
        throw new UnsupportedOperationException("unimplemented");
    }

    default void forEachRemaining(ByteConsumer b) {
        while(hasNext()) {
            b.accept(nextByte());
        }
    }
}
