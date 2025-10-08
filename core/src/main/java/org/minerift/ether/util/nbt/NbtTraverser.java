package org.minerift.ether.util.nbt;

import io.netty.buffer.ByteBuf;
import org.minerift.ether.debug.Debug;
import org.minerift.ether.util.Note;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.function.Predicate;

import static org.minerift.ether.util.nbt.tags.TagTypes.END;

public abstract class NbtTraverser {

    @Debug public static final Predicate<TagHeader> TEST_TAG_SELECTOR = (header) -> Set.of("Data", "BorderSafeZone", "SpawnAngle", "LevelName", "Time").contains(header.name());
    public static final NbtOption[] NO_OPTIONS = new NbtOption[0];

    public final ByteBuf buf;
    public final Predicate<TagHeader> tagSelector; // true for tags to be read, false for tags to be ignored
    protected final NbtOption[] options;

    // FIXME: review byte ordering bullshit later
    public NbtTraverser(ByteBuf buf/*, boolean bigEndian*/, Predicate<TagHeader> tagSelector, NbtOption ... options) {
        this.buf = buf;
        //this.buf = this.buf.order(bigEndian ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
        this.tagSelector = tagSelector;
        this.options = options;
    }

    public <T extends NbtOption> T get(Class<T> option) {
        for(NbtOption o : options) {
            if(o.getClass() == option) {
                return (T) o;
            }
        }
        return null;
    }

    // finds the first NbtOption of the desired class
    public boolean supports(Class<? extends NbtOption> option) {
        for(NbtOption o : options) {
            if(o.getClass() == option) {
                return true;
            }
        }
        return false;
    }

    public boolean supports(Enum<? extends NbtOption> flag) {
        for(NbtOption o : options) {
            if(o == flag) {
                return true;
            }
        }
        return false;
    }

    @SafeVarargs
    public final boolean supports(Enum<? extends NbtOption>... flags) {
        for(Enum<? extends NbtOption> flag : flags) {
            if(!supports(flag)) {
                return false;
            }
        }
        return true;
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

    // skips the number of bytes for the reader index
    @Note("For NbtWriter, if we want to implement the skip functionality we can ")
    public void skip(int bytes) {
        buf.readerIndex(buf.readerIndex() + bytes);
    }

    public byte readByte() {
        return buf.readByte();
    }

    public void readBytes(byte[] dst) {
        buf.readBytes(dst);
    }

    public void readShorts(short[] dst) {
        buf.nioBuffer().asShortBuffer().get(dst);
        buf.skipBytes(Short.BYTES * dst.length);
    }

    public void readInts(int[] dst) {
        buf.nioBuffer().asIntBuffer().get(dst);
        buf.skipBytes(Integer.BYTES * dst.length);
    }

    public void readLongs(long[] dst) {
        buf.nioBuffer().asLongBuffer().get(dst);
        buf.skipBytes(Long.BYTES * dst.length);
    }

    public void readFloats(float[] dst) {
        buf.nioBuffer().asFloatBuffer().get(dst);
        buf.skipBytes(Float.BYTES * dst.length);
    }

    public void readDoubles(double[] dst) {
        buf.nioBuffer().asDoubleBuffer().get(dst);
        buf.skipBytes(Double.BYTES * dst.length);
    }

    public byte[] readBytes(int bytes) {
        byte[] dst = new byte[bytes];
        readBytes(dst);
        return dst;
    }

    public int[] readInts(int ints) {
        int[] dst = new int[ints];
        readInts(dst);
        return dst;
    }

    public long[] readLongs(int longs) {
        long[] dst = new long[longs];
        readLongs(dst);
        return dst;
    }

    public short readShort() {
        return buf.readShort();
    }

    public int readInt() {
        return buf.readInt();
    }

    public long readLong() {
        return buf.readLong();
    }

    public String readUTF8() {
        short len = readShort();
        byte[] bytes = readBytes(len);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public float readFloat() {
        return buf.readFloat();
    }

    public double readDouble() {
        return buf.readDouble();
    }

    public void write(byte[] bytes) {
        buf.writeBytes(bytes);
    }

    public void writeByte(byte b) {
        buf.writeByte(b);
    }

    public void writeShort(short s) {
        buf.writeShort(s);
    }

    public void writeInt(int i) {
        buf.writeInt(i);
    }

    public void writeLong(long l) {
        buf.writeLong(l);
    }

    public void writeFloat(float f) {
        buf.writeFloat(f);
    }

    public void writeDouble(double d) {
        buf.writeDouble(d);
    }

    public void writeUTF8(String str) {
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        buf.writeShort((short) bytes.length);
        buf.writeBytes(bytes);
    }

    public void writeShortArray(short[] shorts) {
        ByteBuffer jbuf = ByteBuffer.allocate(Short.BYTES * shorts.length);
        jbuf.asShortBuffer().put(shorts);
        buf.writeBytes(jbuf);
    }

    public void writeIntArray(int[] ints) {
        //PooledByteBufAllocator.DEFAULT // <- note for pooled netty bufs
        ByteBuffer jbuf = ByteBuffer.allocate(Integer.BYTES * ints.length);
        jbuf.asIntBuffer().put(ints); // doesn't move cursor in main byte buf
        buf.writeBytes(jbuf);
    }

    public void writeLongArray(long[] longs) {
        ByteBuffer jbuf = ByteBuffer.allocate(Long.BYTES * longs.length);
        jbuf.asLongBuffer().put(longs);
        buf.writeBytes(jbuf);
    }

    public void writeFloatArray(float[] floats) {
        ByteBuffer jbuf = ByteBuffer.allocate(Float.BYTES * floats.length);
        jbuf.asFloatBuffer().put(floats);
        buf.writeBytes(jbuf);
    }

    public void writeDoubleArray(double[] doubles) {
        ByteBuffer jbuf = ByteBuffer.allocate(Double.BYTES * doubles.length);
        jbuf.asDoubleBuffer().put(doubles);
        buf.writeBytes(jbuf);
    }

    @Override
    public String toString() {
        return "NbtTraverser{" +
                "buffer=" + buf +
                ", tagSelector=" + tagSelector +
                '}';
    }

    protected static abstract class CommonBuilder<T extends NbtTraverser> {



    }
}
