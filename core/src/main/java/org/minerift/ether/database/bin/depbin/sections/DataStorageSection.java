package org.minerift.ether.database.bin.depbin.sections;

import org.minerift.ether.database.bin.depbin.reader.ReaderContext;
import org.minerift.ether.database.bin.depbin.writer.WriterContext;

import java.nio.ByteBuffer;

public class DataStorageSection implements Section {
    @Override
    public void visit(WriterContext ctx) {
        ByteBuffer serializedData = ByteBuffer.allocate(1024); // TODO: estimate size to allocate


    }

    @Override
    public void visit(ReaderContext ctx) {

    }
}
