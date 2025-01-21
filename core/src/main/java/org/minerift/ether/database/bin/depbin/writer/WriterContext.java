package org.minerift.ether.database.bin.depbin.writer;

import org.minerift.ether.database.DataType;
import org.minerift.ether.database.Database;
import org.minerift.ether.database.bin.depbin.sections.DataStorageSection;
import org.minerift.ether.database.bin.depbin.sections.Section;
import org.minerift.ether.database.bin.depbin.sections.TablesSection;
import org.minerift.ether.database.Ref;
import org.minerift.ether.database.bin.depbin.Context;
import org.minerift.ether.database.bin.depbin.sections.HeaderSection;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.LinkedList;
import java.util.List;

public class WriterContext implements Context {

    protected RandomAccessFile file;
    protected FileChannel channel;
    protected EncoderRegistry encoders;
    protected ByteBuffer currentBuffer;

    private List<ByteBuffer> buffers;
    private List<WriteEntry> entriesToWrite;
    private int bytesToAllocate;

    public HeaderSection header;
    public TablesSection tableInfo;
    public DataStorageSection records;

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
    }

    public <T> WriterContext write(DataType<T> type, Ref<T> ref) {
        return write(type, ref, null);
    }

    public <T> WriterContext write(DataType<T> type, T data) {
        return write(type, data, null);
    }

    public <T> WriterContext write(DataType<T> type, Ref<T> ref, Ref arraySize) {
        WriteEntry<T> entry = new WriteEntry<>(type, ref, arraySize);
        write(entry);
        return this;
    }

    public <T> WriterContext write(DataType<T> type, T data, Ref arraySize) {
        WriteEntry<T> entry = new WriteEntry<>(type, data, arraySize);
        write(entry);
        return this;
    }

    protected void write(WriteEntry<?> entry) {
        entriesToWrite.add(entry);
    }

    // Adds the last write entry size to the size ref
    @Deprecated
    public void referenceSize(Ref<?> size) {
        int byteSize = entriesToWrite.getLast().getByteSize();
        Object sizeVal = size.get();
        switch (sizeVal) {
            case Byte b -> ((Ref<Byte>)size).set((byte)(b + byteSize));
            case Short s -> ((Ref<Short>)size).set((short)(s + byteSize));
            case Integer i -> ((Ref<Integer>)size).set(i + byteSize);
            case Long l -> ((Ref<Long>)size).set(l + byteSize);
            default -> throw new IllegalStateException("Unexpected type: " + sizeVal.getClass());
        }
    }

    public void pushSection() {

    }

    public void submit() {

    }

    @Override
    public void accept(Section section) {
        section.visit(this);
    }
}
