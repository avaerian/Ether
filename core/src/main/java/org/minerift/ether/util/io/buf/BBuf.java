package org.minerift.ether.util.io.buf;

import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;

@Deprecated
public interface BBuf {

    /*static BBuf newBuf() {

    }

    static BBuf from(ByteBuffer javaBuf) {

    }

    static BBuf from(ByteBuf nettyBuf) {

    }*/

    // TODO: replace some overloads with default impl to reduce boilerplate in buf impls

    /*int capacity();
    BBuf capacity(int newCapacity);

    int size();

    // query indices
    int readerIndex();
    int writerIndex();

    // update indices
    void readerIndex(int idx);
    void writerIndex(int idx);

    BBuf write(byte[] bs);
    BBuf writeShorts(short[] ss);
    BBuf writeInts(int[] is);
    BBuf writeLongs(long[] ls);
    BBuf writeFloats(float[] fs);
    BBuf writeDoubles(double[] ds);

    void read(byte[] dst);
    void read(short[] dst);
    void read(int[] dst);
    void read(long[] dst);
    void read(float[] dst);
    void read(double[] dst);

    default byte[] read(int len) {
        byte[] bs = new byte[len];
        read(bs);
        return bs;
    }

    default short[] readShorts(int len) {
        short[] ss = new short[len];
        read(ss);
        return ss;
    }

    default int[] readInts(int len) {
        int[] is = new int[len];
        read(is);
        return is;
    }

    default long[] readLongs(int len) {
        long[] ls = new long[len];
        read(ls);
        return ls;
    }

    default float[] readFloats(int len) {
        float[] fs = new float[len];
        read(fs);
        return fs;
    }

    default double[] readDoubles(int len) {
        double[] ds = new double[len];
        read(ds);
        return ds;
    }

    BBuf writeByte(byte b);
    BBuf writeByte(byte b, int idx);

    BBuf writeShort(short s);
    BBuf writeShort(short s, int idx);

    BBuf writeInt(int i);
    BBuf writeInt(int i, int idx);

    BBuf writeLong(long l);
    BBuf writeLong(long l, int idx);

    BBuf writeFloat(float f);
    BBuf writeFloat(float f, int idx);

    BBuf writeDouble(double d);
    BBuf writeDouble(double d, int idx);

    BBuf writeVarInt(int vi);
    int readVarInt();

    byte readByte();
    byte readByte(int idx);

    short readShort();
    short readShort(int idx);

    int readInt();
    int readInt(int idx);

    long readLong();
    long readLong(int idx);

    float readFloat();
    float readFloat(int idx);

    double readDouble();
    double readDouble(int idx);

    ByteBuffer nioBuffer();

    */

}
