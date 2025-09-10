package org.minerift.ether.util.io.buf;

import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;

public class BBufNetty implements BBuf {

    /*protected interface EndianFnSet {
        void write(byte[] bs);
        void writeShorts(short[] ss);
        void writeInts(int[] is);
        void writeLongs(long[] ls);
        void writeFloats(float[] fs);
        void writeDouble(double[] ds);

        void writeByte(byte b);
        void writeShort(short s);
        void writeInt(int i);
        void writeLong(long l);
        void writeFloat(float f);
        void writeDouble(double d);
    }

    protected EndianFnSet fns;
    protected ByteBuf buf;

    public BBufNetty(ByteBuf buf) {
        buf.order(null);
        this.buf = buf;
    }

    @Override
    public int capacity() {
        return buf.capacity();
    }

    @Override
    public BBuf capacity(int newCapacity) {
        buf.capacity(newCapacity);
        return this;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public int readerIndex() {
        return 0;
    }

    @Override
    public int writerIndex() {
        return 0;
    }

    @Override
    public void readerIndex(int idx) {

    }

    @Override
    public void writerIndex(int idx) {

    }

    @Override
    public BBuf write(byte[] bs) {
        buf.writeBytes(bs);
        return this;
    }

    @Override
    public BBuf writeShorts(short[] ss) {
        return null;
    }

    @Override
    public BBuf writeInts(int[] is) {
        return null;
    }

    @Override
    public BBuf writeLongs(long[] ls) {
        return null;
    }

    @Override
    public BBuf writeFloats(float[] fs) {
        return null;
    }

    @Override
    public BBuf writeDoubles(double[] ds) {
        return null;
    }

    @Override
    public void read(byte[] dst) {

    }

    @Override
    public void read(short[] dst) {

    }

    @Override
    public void read(int[] dst) {

    }

    @Override
    public void read(long[] dst) {

    }

    @Override
    public void read(float[] dst) {

    }

    @Override
    public void read(double[] dst) {

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
        return null;
    }

    @Override
    public BBuf writeByte(byte b, int idx) {
        return null;
    }

    @Override
    public BBuf writeShort(short s) {
        return null;
    }

    @Override
    public BBuf writeShort(short s, int idx) {
        return null;
    }

    @Override
    public BBuf writeInt(int i) {
        return null;
    }

    @Override
    public BBuf writeInt(int i, int idx) {
        return null;
    }

    @Override
    public BBuf writeLong(long l) {
        return null;
    }

    @Override
    public BBuf writeLong(long l, int idx) {
        return null;
    }

    @Override
    public BBuf writeFloat(float f) {
        return null;
    }

    @Override
    public BBuf writeFloat(float f, int idx) {
        return null;
    }

    @Override
    public BBuf writeDouble(double d) {
        return null;
    }

    @Override
    public BBuf writeDouble(double d, int idx) {
        return null;
    }

    @Override
    public BBuf writeVarInt(int vi) {
        return null;
    }

    @Override
    public int readVarInt() {
        return 0;
    }

    @Override
    public byte readByte() {
        return 0;
    }

    @Override
    public byte readByte(int idx) {
        return 0;
    }

    @Override
    public short readShort() {
        return 0;
    }

    @Override
    public short readShort(int idx) {
        return 0;
    }

    @Override
    public int readInt() {
        return 0;
    }

    @Override
    public int readInt(int idx) {
        return 0;
    }

    @Override
    public long readLong() {
        return 0;
    }

    @Override
    public long readLong(int idx) {
        return 0;
    }

    @Override
    public float readFloat() {
        return 0;
    }

    @Override
    public float readFloat(int idx) {
        return 0;
    }

    @Override
    public double readDouble() {
        return 0;
    }

    @Override
    public double readDouble(int idx) {
        return 0;
    }

    @Override
    public ByteBuffer nioBuffer() {
        return buf.nioBuffer();
    }*/
}
