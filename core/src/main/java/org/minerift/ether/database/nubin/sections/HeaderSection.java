package org.minerift.ether.database.nubin.sections;

import org.minerift.ether.database.DataType;
import org.minerift.ether.database.nubin.reader.ReaderContext;
import org.minerift.ether.database.nubin.writer.WriterContext;

public class HeaderSection implements Section {

    public static final byte[] MAGIC = new byte[] { 69, 85, 82, 69, 75, 65, 33 }; // EUREKA!

    @Override
    public void visit(WriterContext ctx) {
        ctx.write(DataType.BINARY(MAGIC.length), MAGIC);
        ctx.submit();
    }

    @Override
    public void visit(ReaderContext ctx) {

    }
}
