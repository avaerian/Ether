package org.minerift.ether.util.nbt;

import com.google.common.base.Predicates;
import org.minerift.ether.debug.Debug;
import org.minerift.ether.util.nbt.tags.PrimitiveTagType;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.function.Predicate;
import java.util.zip.GZIPInputStream;

import static java.nio.ByteOrder.BIG_ENDIAN;
import static java.nio.ByteOrder.LITTLE_ENDIAN;

public class NbtTraverser {


    public static NbtReader from(File f, Compression compress) throws IOException {
        try(RandomAccessFile file = new RandomAccessFile(f, "rw")) {
            byte[] buf = null;
            switch (compress) {
                case NONE -> {
                    buf = new byte[(int) file.length()];
                    file.read(buf);
                }
                case GZIP -> {
                    InputStream is = new GZIPInputStream(new FileInputStream(f));
                    buf = is.readAllBytes();
                }
                case ZLIB -> throw new UnsupportedOperationException("Unimplemented");
            }


            //ByteBuffer buffer = ByteBuffer.allocate((int) file.length());
            //file.getChannel().read(buffer);
            ByteBuffer buffer = ByteBuffer.wrap(buf);
            return new NbtReader(buffer, true);
        }
    }



    public ByteBuffer buffer;
    public Predicate<TagHeader> tagSelector; // true for tags to be read, false for tags to be ignored

    @Debug public static final Predicate<TagHeader> TEST_TAG_SELECTOR = (header) -> Set.of("Data", "BorderSafeZone", "SpawnAngle", "LevelName", "Time").contains(header.name());


    public NbtTraverser(ByteBuffer buffer, boolean bigEndian) {
        this(buffer, bigEndian, Predicates.alwaysTrue());
    }

    public NbtTraverser(ByteBuffer buffer, boolean bigEndian, Predicate<TagHeader> tagSelector) {
        this.buffer = buffer;
        buffer.order(bigEndian ? BIG_ENDIAN : LITTLE_ENDIAN);
        this.tagSelector = tagSelector;
    }

    public record TagHeader(byte id, String name) {

    }

    public TagHeader readTagHeader() {
        byte id = readByte();
        String name = PrimitiveTagType.END.getId() == id ? "" : readUTF8();
        TagHeader header = new TagHeader(id, name);
        return header;
    }

    public void skip(int bytes) {
        buffer.position(buffer.position() + bytes);
    }

    public byte readByte() {
        return buffer.get();
    }

    public void read(byte[] dst) {
        buffer.get(dst);
    }

    public byte[] read(int bytes) {
        byte[] dst = new byte[bytes];
        read(dst);
        return dst;
    }

    public short readShort() {
        return buffer.getShort();
    }

    public int readInt() {
        return buffer.getInt();
    }

    public long readLong() {
        return buffer.getLong();
    }

    public String readUTF8() {
        short len = readShort();
        byte[] bytes = read(len);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public float readFloat() {
        return buffer.getFloat();
    }

    public double readDouble() {
        return buffer.getDouble();
    }

    // for writing; TODO
    protected void ensureCapacity(int bytes) {
        if(buffer.position() + bytes > buffer.capacity()) {

        }
    }

    public void write(byte[] bytes) {
        buffer.put(bytes);
    }

    public void writeByte(byte b) {
        buffer.put(b);
    }

    public void writeShort(short s) {
        buffer.putShort(s);
    }

    public void writeInt(int i) {
        buffer.putInt(i);
    }

    public void writeLong(long l) {
        buffer.putLong(l);
    }

    public void writeFloat(float f) {
        buffer.putFloat(f);
    }

    public void writeDouble(double d) {
        buffer.putDouble(d);
    }

    public void writeUTF8(String str) {
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        buffer.putShort((short) bytes.length);
        buffer.put(bytes);
    }

    public void writeIntArray(int[] ints) {
        //buffer.putInt(ints.length);
        buffer.asIntBuffer().put(ints);
        skip(Integer.BYTES * ints.length); // correct position in main buffer
    }

    public void writeLongArray(long[] longs) {
        //buffer.putInt(longs.length);
        buffer.asLongBuffer().put(longs);
        skip(Long.BYTES * longs.length); // correct position in main buffer
    }

    @Override
    public String toString() {
        return "NbtTraverser{" +
                "buffer=" + buffer +
                ", tagSelector=" + tagSelector +
                '}';
    }
}
