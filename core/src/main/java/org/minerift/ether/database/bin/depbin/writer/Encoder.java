package org.minerift.ether.database.bin.depbin.writer;

import org.minerift.ether.database.DataType;

public interface Encoder<T> {
    void accept(WriterContext ctx, DataType<T> type, T data);
}
