package org.minerift.ether.util.iterator;

import java.util.Iterator;

public interface ByteIterator extends Iterator<Byte> {

    static ByteIterator of(byte[] bytes) {
        return new ByteIteratorImpl(bytes);
    }

    static ByteIterator of(byte[] bytes, int pos) {
        return new ByteIteratorImpl(bytes, pos);
    }

    class ByteIteratorImpl implements ByteIterator {

        private final byte[] bytes;
        private int pos;

        protected ByteIteratorImpl(byte[] bytes) {
            this(bytes, 0);
        }

        protected ByteIteratorImpl(byte[] bytes, int pos) {
            this.bytes = bytes;
            this.pos = pos;
        }

        @Override
        public byte nextByte() {
            return bytes[pos++];
        }

        @Override
        public boolean hasNext() {
            return pos < bytes.length;
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
