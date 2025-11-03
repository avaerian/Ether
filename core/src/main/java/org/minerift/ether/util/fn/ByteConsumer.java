package org.minerift.ether.util.fn;

import java.util.function.Consumer;

public interface ByteConsumer extends Consumer<Byte> {
    void accept(byte b);

    @Deprecated
    @Override
    default void accept(Byte b) {
        accept(b.byteValue());
    }
}
