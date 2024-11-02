package org.minerift.ether.database.nubin.sections;

import org.minerift.ether.database.Model;
import org.minerift.ether.database.nubin.reader.ReaderContext;
import org.minerift.ether.database.nubin.writer.WriterContext;

public class TablesSection implements Section {

    @Override
    public void visit(WriterContext ctx) {
        Model[] models = new Model[];
        ctx.db
    }

    @Override
    public void visit(ReaderContext ctx) {

    }
}
