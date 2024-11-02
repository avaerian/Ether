package org.minerift.ether.database.nubin;

import org.minerift.ether.database.nubin.reader.ReaderContext;
import org.minerift.ether.database.nubin.writer.WriterContext;

public interface ContextVisitor {
    void visit(WriterContext ctx);
    void visit(ReaderContext ctx);
}
