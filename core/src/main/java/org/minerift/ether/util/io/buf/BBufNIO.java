package org.minerift.ether.util.io.buf;

import org.minerift.ether.debug.Debug;
import org.minerift.ether.util.UnreachableException;

import java.nio.ByteBuffer;

public class BBufNIO implements BBuf {

    /*public static final int NO_FLAGS = 0;
    public static final int REALLOC_DIRECT = 1;

    protected int readIdx;
    protected int writeIdx;
    protected ByteBuffer buf;

    public BBufNIO(ByteBuffer buf) {
        this.buf = buf;
    }

    @Override
    public int capacity() {
        return buf.capacity();
    }

    // TODO: review; may move to have this as extended behavior
    @Override
    public BBuf capacity(int newCapacity) {
        /*if(newCapacity > buf.capacity()) {
            ByteBuffer copy = buf.
        } //
        throw new UnreachableException("unimplemented");
    }

    @Override
    public int size() {
        return buf.limit();
    }

    @Override
    public int readerIndex() {
        return readIdx;
    }

    @Override
    public int writerIndex() {
        return writeIdx;
    }

    @Override
    public void readerIndex(int idx) {
        this.readIdx = idx;
    }

    @Override
    public void writerIndex(int idx) {
        this.writeIdx = idx;
    }

    @Override
    public BBuf write(byte[] bs) {
        buf.put(writeIdx, bs);
        writeIdx += bs.length;
        return this;
    }

    @Override
    public BBuf writeShorts(short[] ss) {
        buf.asShortBuffer()
                .put( writeIdx, ss, 0, ss.length );
        writeIdx += Short.BYTES * ss.length;
        return this;
    }

    @Debug
    public static void main(String[] args) {
        int idx = 0;
        System.out.println((idx=idx+5));
        System.out.println(idx);
    }

    @Override
    public BBuf writeInts(int[] is) {
        buf.asIntBuffer()
                .put(writeIdx, is, 0, is.length);
        writeIdx += Integer.BYTES * is.length;
        return this;
    }

    @Override
    public BBuf writeLongs(long[] ls) {
        buf.asLongBuffer()
                .put( writeIdx, ls, 0, ls.length);
        writeIdx += Long.BYTES * ls.length;
        return this;
    }

    @Override
    public BBuf writeFloats(float[] fs) {
        buf.asFloatBuffer()
                .put( writeIdx, fs, 0, fs.length);
        writeIdx += Float.BYTES * fs.length;
        return this;
    }

    @Override
    public BBuf writeDoubles(double[] ds) {
        buf.asDoubleBuffer()
                .put( writeIdx, ds, 0, ds.length);
        writeIdx += Double.BYTES * ds.length;
        return this;
    }

    @Override
    public void read(byte[] dst) {
        buf.get(readIdx, dst);
        readIdx += dst.length;
    }

    @Override
    public void read(short[] dst) {
        buf.asShortBuffer()
                .get(readIdx, dst);
        readIdx += Short.BYTES * dst.length;
    }

    @Override
    public void read(int[] dst) {
        buf.asIntBuffer()
                .get(readIdx, dst);
        readIdx += Integer.BYTES * dst.length;
    }

    @Override
    public void read(long[] dst) {
        buf.asLongBuffer()
                .get(readIdx, dst);
        readIdx += Long.BYTES * dst.length;
    }

    @Override
    public void read(float[] dst) {
        buf.asFloatBuffer()
                .get(readIdx, dst);
        readIdx += Float.BYTES * dst.length;
    }

    @Override
    public void read(double[] dst) {
        buf.asDoubleBuffer()
                .get(readIdx, dst);
        readIdx += Double.BYTES * dst.length;
    }

    @Override
    public byte[] read(int len) {
        return new byte[0];
    }

    @Override
    public short[] readShorts(int len) {
        return new short[0];
    }

    @Override
    public int[] readInts(int len) {
        return new int[0];
    }

    @Override
    public long[] readLongs(int len) {
        return new long[0];
    }

    @Override
    public float[] readFloats(int len) {
        return new float[0];
    }

    @Override
    public double[] readDoubles(int len) {
        return new double[0];
    }

    @Override
    public BBuf writeByte(byte b) {
        return writeByte(b, writeIdx++);
    }

    @Override
    public BBuf writeByte(byte b, int idx) {
        buf.put(idx, b);
        return this;
    }

    @Override
    public BBuf writeShort(short s) {
        writeShort(s, writeIdx);
        readIdx += Short.BYTES;
        return this;
    }

    @Override
    public BBuf writeShort(short s, int idx) {
        buf.putShort(idx, s);
        return this;
    }

    @Override
    public BBuf writeInt(int i) {
        writeInt(i, writeIdx);
        writeIdx += Integer.BYTES;
        return this;
    }

    @Override
    public BBuf writeInt(int i, int idx) {
        buf.putInt(idx, i);
        return this;
    }

    @Override
    public BBuf writeLong(long l) {
        writeLong(l, writeIdx);
        writeIdx += Long.BYTES;
        return this;
    }

    @Override
    public BBuf writeLong(long l, int idx) {
        buf.putLong(idx, l);
        return this;
    }

    @Override
    public BBuf writeFloat(float f) {
        writeFloat(f, writeIdx);
        writeIdx += Float.BYTES;
        return this;
    }

    @Override
    public BBuf writeFloat(float f, int idx) {
        buf.putFloat(idx, f);
        return this;
    }

    @Override
    public BBuf writeDouble(double d) {
        writeDouble(d, writeIdx);
        writeIdx += Double.BYTES;
        return this;
    }

    @Override
    public BBuf writeDouble(double d, int idx) {
        buf.putDouble(idx, d);
        return this;
    }

    // TODO
    @Override
    public BBuf writeVarInt(int vi) {
        return null;
    }

    // TODO
    @Override
    public int readVarInt() {
        return 0;
    }

    @Override
    public byte readByte() {
        return readByte(readIdx++);
    }

    @Override
    public byte readByte(int idx) {
        return buf.get(idx);
    }

    @Override
    public short readShort() {
        short s = readShort(readIdx);
        readIdx += Short.BYTES;
        return s;
    }

    @Override
    public short readShort(int idx) {
        return buf.getShort(idx);
    }

    @Override
    public int readInt() {
        int i = readInt(readIdx);
        readIdx += Integer.BYTES;
        return i;
    }

    @Override
    public int readInt(int idx) {
        return buf.getInt(idx);
    }

    @Override
    public long readLong() {
        long l = readLong(readIdx);
        readIdx += Long.BYTES;
        return l;
    }

    @Override
    public long readLong(int idx) {
        return buf.getLong(idx);
    }

    @Override
    public float readFloat() {
        float f = readFloat(readIdx);
        readIdx += Float.BYTES;
        return f;
    }

    @Override
    public float readFloat(int idx) {
        return buf.getFloat(idx);
    }

    @Override
    public double readDouble() {
        double d = readDouble(readIdx);
        readIdx += Double.BYTES;
        return d;
    }

    @Override
    public double readDouble(int idx) {
        return buf.getDouble(idx);
    }

    @Override
    public ByteBuffer nioBuffer() {
        return buf;
    }*/
}
