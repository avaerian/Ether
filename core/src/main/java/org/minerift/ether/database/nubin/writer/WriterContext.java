package org.minerift.ether.database.nubin.writer;

import org.minerift.ether.database.DataType;
import org.minerift.ether.database.Database;
import org.minerift.ether.database.bin.Ref;
import org.minerift.ether.database.nubin.Context;
import org.minerift.ether.database.nubin.sections.DataStorageSection;
import org.minerift.ether.database.nubin.sections.HeaderSection;
import org.minerift.ether.database.nubin.sections.Section;
import org.minerift.ether.database.nubin.sections.data.Table;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class WriterContext implements Context {

    protected RandomAccessFile file;
    protected FileChannel channel;
    protected EncoderRegistry encoders;
    protected ByteBuffer currentBuffer;

    private List<ByteBuffer> buffers;
    private List<WriteEntry> entriesToWrite;
    private int bytesToAllocate;

    public HeaderSection header;
    public DataStorageSection records;

    public Map<String, Table> tables;
    public Database db; // TODO


    public WriterContext(File file) throws FileNotFoundException {
        this.file = new RandomAccessFile(file, "rw");
        this.channel = this.file.getChannel();
        this.encoders = new EncoderRegistry.Builder()
                .register(DataType.BOOL, Encoders::writeBool)
                .register(DataType.BYTE, Encoders::writeByte)
                .register(DataType.SHORT, Encoders::writeShort)
                .register(DataType.INT, Encoders::writeInt)
                .register(DataType.BIGINT, Encoders::writeLong)
                .register(DataType.FLOAT, Encoders::writeFloat)
                .register(DataType.DOUBLE, Encoders::writeDouble)
                .register(DataType.CHAR, Encoders::writeChar)
                .register(DataType.VARCHAR, Encoders::writeVarchar)
                .register(DataType.BINARY, Encoders::writeBinary)
                .register(DataType.VARBINARY, Encoders::writeBinary)
                //.register(DataType.UUIDv4, null)
                .build();
        this.entriesToWrite = new LinkedList<>();

        this.tables = new HashMap<>();
    }

    public <T> void write(DataType<T> type, Ref<T> ref) {

    }

    public <T> void write(DataType<T> type, T data) {

    }

    public void submit() {

    }

    @Override
    public void accept(Section section) {
        section.visit(this);
    }
}
