package org.minerift.ether.util.nbt;

import com.google.common.base.Predicates;
import com.google.common.io.LittleEndianDataInputStream;
import org.minerift.ether.debug.Debug;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.function.Predicate;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

import static java.nio.ByteOrder.BIG_ENDIAN;
import static java.nio.ByteOrder.LITTLE_ENDIAN;
import static org.minerift.ether.util.nbt.tags.TagTypes.END;

public class NbtTraverser {

    // references https://isc.sans.edu/diary/25182
    // tentatively check for zlib/gzip compression
    public static NbtReader from(File f) throws IOException {
        try(RandomAccessFile file = new RandomAccessFile(f, "r")) {
            short magic = file.readShort(); // big-endian
            final Compression cmps;
            if(magic == 0x7801 /* zlib no/low compression */
                    || magic == 0x789c /* zlib default compression */
                    || magic == 0x78da /* zlib best compression */) {
                cmps = Compression.ZLIB;
            } else if(magic == 0x1f8b) { /* gzip compression */
                cmps = Compression.GZIP;
            } else {
                cmps = Compression.NONE;
            }

            file.seek(0);
            return from(file, cmps);
        }
    }

    // Doesn't close the RandomAccessFile
    // NOTE: it seems GZipInputStream (GZIPInputStream.GZIP_MAGIC specifically)
    // uses little-endian (Intel) byte ordering, so keep that in-mind.
    protected static NbtReader from(RandomAccessFile file, Compression compress) throws IOException {
        byte[] _buf;
        switch (compress) {
            case NONE -> {
                _buf = new byte[(int) file.length()]; // handle this in the future? should never expect a file this big
                file.read(_buf);
            }
            case GZIP -> {
                InputStream is = new GZIPInputStream(new FileInputStream(file.getFD()));
                _buf = is.readAllBytes();
            }
            case ZLIB -> { // needs testing
                InputStream is = new InflaterInputStream(new FileInputStream(file.getFD()));
                _buf = is.readAllBytes();
            }
            default -> throw new IllegalStateException("Unexpected value: " + compress);
        }

        ByteBuffer buf = ByteBuffer.wrap(_buf);
        return new NbtReader(buf, true);
    }

    public static NbtReader from(File f, Compression compress) throws IOException {
        try(RandomAccessFile file = new RandomAccessFile(f, "r")) {
            return from(file, compress);
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
        // empty
    }

    public TagHeader readTagHeader() throws NbtReadException {
        byte id = readByte();
        String name = END.getId() == id ? "" : readUTF8();
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
