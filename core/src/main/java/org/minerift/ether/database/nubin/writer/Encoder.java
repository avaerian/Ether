package org.minerift.ether.database.nubin.writer;

import org.minerift.ether.database.DataType;
import org.minerift.ether.database.bin.Ref;
import org.minerift.ether.util.TriConsumer;

public interface Encoder<T> {
    void accept(WriterContext ctx, DataType<T> type, T data);
}
