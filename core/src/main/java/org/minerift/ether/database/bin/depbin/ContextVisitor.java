package org.minerift.ether.database.bin.depbin;

import org.minerift.ether.database.bin.depbin.reader.ReaderContext;
import org.minerift.ether.database.bin.depbin.writer.WriterContext;

public interface ContextVisitor {
    void visit(WriterContext ctx);
    void visit(ReaderContext ctx);
}
