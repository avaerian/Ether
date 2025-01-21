package org.minerift.ether.database.bin.writer;

import org.minerift.ether.database.DataType;
import org.minerift.ether.database.bin.BinaryContext;
import org.minerift.ether.database.Ref;
import org.minerift.ether.util.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.LinkedList;
import java.util.List;

import static org.minerift.ether.util.Utils.*;

public class BinaryWriterContext extends BinaryContext {

    public static final int EXPECTING_SECTION_SIZE = -1;
    public static final int ON_STANDBY_SECTION_SIZE = -2;

    //private final EncoderRegistry encoders;
    private final RandomAccessFile file;
    private final FileChannel channel;
    protected ByteBuffer currentBuffer;
    private List<ByteBuffer> buffers;
    private List<WriteEntry> entriesToWrite;
    private int bytesToAllocate;
    private String sectionName;
    private int sectionSizeEntryIndex;
    private boolean allowsNulls, prevAllowsNulls;

    // TODO: need to store state (stack of states? I'm thinking this might be the approach)

    public BinaryWriterContext(File file) throws FileNotFoundException {
        this.file = new RandomAccessFile(file, "rw");
        this.channel = this.file.getChannel();
        /*this.encoders = new EncoderRegistry.Builder()
                .register(DataType.BOOL, this::writeBool)
                .register(DataType.BYTE, this::writeByte)
                .register(DataType.SHORT, this::writeShort)
                .register(DataType.INT, this::writeInt)
                .register(DataType.BIGINT, this::writeLong)
                .register(DataType.FLOAT, this::writeFloat)
                .register(DataType.DOUBLE, this::writeDouble)
                .register(DataType.CHAR, this::writeChar)
                .register(DataType.VARCHAR, this::writeVarchar)
                .register(DataType.BINARY, this::writeBinary)
                .register(DataType.VARBINARY, this::writeBinary)
                //.register(DataType.UUIDv4, null)
                .build();*/
        this.entriesToWrite = new LinkedList<>();
        this.buffers = new LinkedList<>();
        this.bytesToAllocate = 0;
        this.sectionName = null;
        this.sectionSizeEntryIndex = ON_STANDBY_SECTION_SIZE;
        this.allowsNulls = false;
        this.prevAllowsNulls = false;
    }

    protected boolean expectingSectionSize() {
        return sectionSizeEntryIndex == EXPECTING_SECTION_SIZE;
    }

    @Override
    public <T> void bin(DataType<T> type, Ref<T> val, Ref arraySize) {
        // for reader, read type bytes and set ref to that
        // for writer, get ref val and write based on type data
        if(val.get() == null && !stateAllowsNulls()) {
            throw new RuntimeException("Value is null when it shouldn't be");
        }

        if(expectingSectionSize()) {
            if(!type.isIntegerType()) {
                throw new RuntimeException("Expecting an integer section size, but instead got a " + type.getPrimitiveType());
            }
            this.sectionSizeEntryIndex = entriesToWrite.size();
            allowNulls(prevAllowsNulls);
        }

        WriteEntry<T> entry = new WriteEntry<>(type, val, arraySize);
        entriesToWrite.add(entry);
        bytesToAllocate += entry.getByteSize(); // TODO: fix up for calculating static/dynamic array size bytes; move logic to WriteEntry?
    }

    @Override
    public void markSectionSize() {
        this.sectionSizeEntryIndex = EXPECTING_SECTION_SIZE;
        allowNulls(true);
    }

    @Override
    public void beginSection(String section) {
        if(sectionName != null) {
            throw new UnsupportedOperationException("Section " + sectionName + " is not complete!");
        }
        if(section.isBlank()) {
            throw new IllegalArgumentException("Section name should not be blank!");
        }

        this.sectionName = section;
    }

    @Override
    public void markSectionDone() {
    /*
        // TODO: handle based on current state
        if(sectionSizeEntryIndex >= 0) {
            var entry = entriesToWrite.get(sectionSizeEntryIndex);
            entry.data.set(int16ToUnknownInt(getSectionSize(), entry.type.getPrimitiveType()));
        }

        ByteBuffer buffer = ByteBuffer.allocate(bytesToAllocate);
        System.out.println(bytesToAllocate);
        this.currentBuffer = buffer;
        for(WriteEntry entry : entriesToWrite) {
            if(entry.data.get() == null && !entry.type.isNullable()) {
                throw new RuntimeException("Value is disallowed based on type " + entry.type.getPrimitiveType());
            }

            var encoder = encoders.getEncoder(entry.type);
            if(entry.isArray()) {
                // write size, if necessary
                if(entry.hasDynamicArrayLength()) {
                    encoders.getEncoder(DataType.INT).accept(this, entry.type, entry.arraySize);
                }

                // write elements
                int arraySize = entry.getArrayLength();
                Ref element = new Ref();
                for(int i = 0; i < arraySize; i++) {
                    element.set(getArrayElement(entry.data.get(), i));
                    encoder.accept(this, entry.type, element);
                }
            } else {
                encoder.accept(this, entry.type, entry.data);
            }
        }
        */
        //buffer.flip();
        //buffers.add(buffer);
        this.currentBuffer = null;
        this.entriesToWrite.clear();
        this.bytesToAllocate = 0;
        this.sectionName = null;
        this.sectionSizeEntryIndex = ON_STANDBY_SECTION_SIZE;
    }

    @Override
    public void markComplete() {
        // Dump everything to file
        try {
            //long bytes = channel.write(buffers.toArray(ByteBuffer[]::new));
            //System.out.println("Wrote " + bytes + " bytes");
            for(ByteBuffer buffer : buffers) {
                long bytes = channel.write(buffer);
                System.out.println("Wrote " + bytes + " bytes, buffer size " + buffer.array().length);
            }
            channel.close();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public boolean isWriter() {
        return true;
    }

    @Override
    public boolean isReader() {
        return false;
    }

    // Section size is the sizes of the entries after the size entry, including the size entry
    @Override
    public int getSectionSize() {
        int size = 0;
        int i = 0;
        for(var it = entriesToWrite.iterator(); it.hasNext(); i++) {
            if(i < sectionSizeEntryIndex) {
                continue;
            }
            WriteEntry entry = it.next();
            size += entry.getByteSize();
        }
        return size;
    }

    @Override
    public void allowNulls(boolean nullable) {
        this.prevAllowsNulls = allowsNulls;
        this.allowsNulls = nullable;
    }

    @Deprecated
    protected static class WriteEntry<T> {
        protected final DataType<T> type;
        protected final Ref<T> data;
        protected final Ref arraySize;

        protected WriteEntry(DataType<T> type, Ref<T> data, Ref arraySize) {
            this.type = type;
            this.data = data;
            this.arraySize = arraySize;
        }

        public boolean isArray() {
            return type.isArrayType() && !type.isExtendedBinaryType();
        }

        public boolean hasDynamicArrayLength() {
            return arraySize == null && type.hasDynamicArrayLength();
        }

        public int getArrayLength() {
            if(!type.isArrayType()) {
                throw new RuntimeException("Data type is not an array of " + type.getPrimitiveType());
            }
            if(hasDynamicArrayLength()) {
                return Utils.getArrayLength(data.get());
            }
            return arraySize != null ? unknownIntToInt16(arraySize.get()) : type.arrayLength();
        }

        public int getByteSize() {
            if(!isArray()) {
                return type.getByteSize(data.get());
            }

            int size = getArrayLength();
            int bytes = hasDynamicArrayLength() ? Integer.BYTES : 0;
            Ref element = new Ref();
            for(int i = 0; i < size; i++) {
                element.set(getArrayElement(data.get(), i));
                // TODO: bytes += type.getByteSize(element);
            }
            return bytes;
        }
    }

    protected void writeBool(BinaryWriterContext ctx, DataType<Boolean> type, Ref<Boolean> val) {
        ctx.currentBuffer.put((byte) (val.get() ? 1 : 0));
    }

    protected void writeByte(BinaryWriterContext ctx, DataType<Byte> type, Ref<Byte> val) {
        // TODO: move this if statement to markSectionDone()
        ctx.currentBuffer.put(val.get());
    }

    protected void writeShort(BinaryWriterContext ctx, DataType<Short> type, Ref<Short> val) {
        ctx.currentBuffer.putShort(val.get());
    }

    protected void writeInt(BinaryWriterContext ctx, DataType<Integer> type, Ref<Integer> val) {
        ctx.currentBuffer.putInt(unknownIntToInt16(val.get()));
    }

    protected void writeLong(BinaryWriterContext ctx, DataType<Long> type, Ref<Long> val) {
        ctx.currentBuffer.putLong(val.get());
    }

    protected void writeFloat(BinaryWriterContext ctx, DataType<Float> type, Ref<Float> val) {
        ctx.currentBuffer.putFloat(val.get());
    }

    protected void writeDouble(BinaryWriterContext ctx, DataType<Double> type, Ref<Double> val) {
        ctx.currentBuffer.putDouble(val.get());
    }

    protected void writeChar(BinaryWriterContext ctx, DataType<String> type, Ref<String> val) {
        String str;
        try {
            str = val.get().substring(0, type.length());
        } catch (IndexOutOfBoundsException ex) {
            str = val.get();
            str += " ".repeat(type.length() - str.length());
        }
        ctx.currentBuffer.put(str.getBytes());
    }

    protected void writeVarchar(BinaryWriterContext ctx, DataType<String> type, Ref<String> val) {
        ctx.currentBuffer.putShort((short) val.get().length());
        ctx.currentBuffer.put(val.get().getBytes());
    }

    protected void writeBinary(BinaryWriterContext ctx, DataType<byte[]> type, Ref<byte[]> val) {
        ctx.currentBuffer.put(val.get());
    }

    protected void writeVarbinary(BinaryWriterContext ctx, DataType<byte[]> type, Ref<byte[]> val) {
        ctx.currentBuffer.putInt(val.get().length);
        ctx.currentBuffer.put(val.get());
    }

    public boolean stateAllowsNulls() {
        return allowsNulls;
    }


}
