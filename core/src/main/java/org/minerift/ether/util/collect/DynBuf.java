package org.minerift.ether.util.collect;

import org.minerift.ether.debug.Debug;
import org.minerift.ether.debug.Experimental;
import org.minerift.ether.debug.NeedsTesting;
import org.minerift.ether.math.Vec2i;

import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;
import java.util.function.IntUnaryOperator;

@SuppressWarnings("Duplicates")
@Experimental
@NeedsTesting // write unit tests for this class
public class DynBuf {
    // Thread safety is also a desire

    public static final int CHUNK_SIZE = 4096;
    public static final int MAX_VARINT_BYTES = 10;
    public static final IntUnaryOperator DEFAULT_GROWER = (c) -> (int)(c * 1.5f); // OR (c) -> c + (c >> 1)


    private static final int BYTE = 0;
    private static final int SHORT = 1;
    private static final int INT = 2;
    private static final int LONG = 3;
    private static final int FLOAT = 4;
    private static final int DOUBLE = 5;


    private byte[] buf;
    private int len;
    private int cursor; // TODO: refactor into reader/writer cursors?
    private IntUnaryOperator grower;
    private volatile Queue<Vec2i> lockedRegions;
    private final ByteOrder order;
    private final int endianByteShift;
    private final int[] endianByteInitPos;

    // TODO: pooling?
    // TODO: move all ctors to static methods and create single all-parameter ctor

    @Debug
    public static DynBuf createChunkBuf() { // example
        return create(CHUNK_SIZE, (capacity) -> capacity + CHUNK_SIZE);
    }

    @Debug
    public static DynBuf createTestBuf() {
        return create(32);
    }


    public static DynBuf create(int capacity, IntUnaryOperator grower) {
        return new DynBuf(new byte[capacity], 0, 0, grower);
    }

    public static DynBuf create(int capacity) {
        return new DynBuf(new byte[capacity], 0, 0, DEFAULT_GROWER);
    }

    // Wraps the raw byte array in a DynBuf
    public static DynBuf wrap(byte[] bytes, int len) {
        return new DynBuf(bytes, 0, len, DEFAULT_GROWER);
    }

    private DynBuf(byte[] bytes, int offset, int len, IntUnaryOperator grower) {
        this.buf = bytes;
        this.len = len;
        this.cursor = offset;
        this.lockedRegions = new ArrayDeque<>();
        this.grower = grower;

        this.order = ByteOrder.BIG_ENDIAN;
        this.endianByteShift = order == ByteOrder.LITTLE_ENDIAN ? 8 : -8;
        this.endianByteInitPos = new int[6];
        if(order == ByteOrder.BIG_ENDIAN) {
            endianByteInitPos[BYTE] = 0; // (Byte.BYTES - 1) * 8 = 0
            endianByteInitPos[SHORT] = (Short.BYTES - 1) * 8;
            endianByteInitPos[INT] = (Integer.BYTES - 1) * 8;
            endianByteInitPos[LONG] = (Long.BYTES - 1) * 8;
            endianByteInitPos[FLOAT] = (Float.BYTES - 1) * 8;
            endianByteInitPos[DOUBLE] = (Double.BYTES - 1) * 8;
        }

        System.out.println(Arrays.toString(endianByteInitPos));
    }

    public ByteOrder endianness() {
        return order;
    }

    public int capacity() {
        return buf.length;
    }

    public DynBuf write(byte[] bytes) {
        ensureCapacityFor(bytes.length);
        System.arraycopy(bytes, 0, buf, len, bytes.length);
        cursor += bytes.length;
        if(cursor > len) len = cursor;
        return this;
    }

    public byte[] read(int len) {
        byte[] bytes = new byte[len];
        System.arraycopy(buf, cursor, bytes, 0, len);
        cursor += len;
        return bytes;
    }

    public void writeByte(byte b) {
        ensureCapacityFor(Byte.BYTES);
        buf[cursor++] = b;
        if(cursor > len) len = cursor;
    }
    
    public byte readByte() {
        byte result = buf[cursor++];
        return result;
    }

    public DynBuf writeShort(short s) {
        ensureCapacityFor(Short.BYTES);
        int shift = endianByteInitPos[SHORT];

        buf[cursor++] = (byte) (s >> shift & 0xFF);
        buf[cursor++] = (byte) (s >> (shift += endianByteShift) & 0xFF);

        if(cursor > len) len = cursor;
        return this;
    }

    public short readShort() {
        short result = 0;
        int shift = endianByteInitPos[SHORT];

        result |= (short) (((buf[cursor++] & 0xFF) << shift)
                | ((buf[cursor++] & 0xFF) << (shift += endianByteShift)));
        
        return result;
    }

    public DynBuf writeInt(int i) {
        ensureCapacityFor(Integer.BYTES);
        int shift = endianByteInitPos[INT];

        buf[cursor++] = (byte) (i >> shift & 0xFF);
        buf[cursor++] = (byte) (i >> (shift += endianByteShift) & 0xFF);
        buf[cursor++] = (byte) (i >> (shift += endianByteShift) & 0xFF);
        buf[cursor++] = (byte) (i >> (shift += endianByteShift) & 0xFF);

        if(cursor > len) len = cursor;
        return this;
    }

    public int readInt() {
        int result = 0;
        int shift = endianByteInitPos[INT];

        result |= (buf[cursor++] & 0xFF) << shift
        | (buf[cursor++] & 0xFF) << (shift += endianByteShift)
        | (buf[cursor++] & 0xFF) << (shift += endianByteShift)
        | (buf[cursor++] & 0xFF) << (shift += endianByteShift);

        /*result |= (buf[cursor++] & 0xFF) << shift;
        result |= (buf[cursor++] & 0xFF) << (shift += endianByteShift);
        result |= (buf[cursor++] & 0xFF) << (shift += endianByteShift);
        result |= (buf[cursor++] & 0xFF) << (shift += endianByteShift);*/

        return result;
    }

    // Encodes an int with the "ZigZag" pattern -> https://protobuf.dev/programming-guides/encoding/#varints
    protected int encodeZigZagInt(int i) {
        return i >= 0
                ? i << 1 // 2 * i
                : (i << 1) ^ (i >> 31);
    }

    protected long encodeZigZagLong(long i) {
        return i >= 0
                ? i << 1 // 2 * i
                : (i << 1) ^ (i >> 63);
    }

    // https://github.com/protocolbuffers/protobuf/blob/main/csharp/src/Google.Protobuf/ParsingPrimitives.cs#L744
    protected int decodeZigZagInt(int i) {
        return (i >>> 1) ^ -(i & 1);
        /*return (result & 1) == 0 // if LSB (1) is off -> even -> positive
                ? result >> 1
                : (result >> 1) ^ -(result & 1);*/
    }

    protected long decodeZigZagLong(long i) {
        return (i >>> 1) ^ -(i & 1);
    }

    public DynBuf writeVarIntSigned(int i) {
        return writeVarInt(encodeZigZagInt(i));
    }

    public int readVarIntSigned() {
        return decodeZigZagInt(readVarInt());
    }

    // TODO: ChunkedMMappedFile (memory mapped file that supports chunking) ????
    @Debug
    public static void main(String[] args) {

        //int test = Short.MAX_VALUE;
        int test = -420;

        DynBuf buffer = DynBuf.createChunkBuf();
        System.out.println("Initial buffer:");
        System.out.println(buffer);

        buffer.writeInt(test);

        System.out.println("Write:" + test);
        System.out.println(buffer);

        buffer.cursor = 0;

        System.out.println("Read: " + buffer.readInt());
        System.out.println(buffer);

        ///////////////////////////
        buffer = DynBuf.createChunkBuf();
        System.out.println("\nInitial buffer:");
        System.out.println(buffer);

        buffer.writeVarInt(test);

        System.out.println("VarInt Write:" + test);
        System.out.println(buffer);

        buffer.cursor = 0;

        System.out.println("VarInt Read: " + buffer.readVarInt());
        System.out.println(buffer);


        ///////////////////////////
        buffer = DynBuf.createTestBuf();
        System.out.println("\nInitial buffer:");
        System.out.println(buffer);

        buffer.writeVarIntSigned(test);

        System.out.println("Write signed int: " + test);
        System.out.println(Integer.toBinaryString(test));
        System.out.println(buffer);

        buffer.cursor = 0;

        int res = buffer.readVarIntSigned();
        System.out.println("Read signed int: " + res);
        System.out.println(Integer.toBinaryString(res));
        System.out.println(buffer);

        ///////////////////////////
        buffer = DynBuf.createTestBuf();
        System.out.println("\nInitial buffer:");
        System.out.println(buffer);

        // TODO: move these to Unit Tests

    }

    public DynBuf writeLong(long l) {
        ensureCapacityFor(Long.BYTES);
        int shift = endianByteInitPos[LONG];

        buf[cursor++] = (byte) (l >> shift & 0xFF);
        buf[cursor++] = (byte) (l >> (shift += endianByteShift) & 0xFF);
        buf[cursor++] = (byte) (l >> (shift += endianByteShift) & 0xFF);
        buf[cursor++] = (byte) (l >> (shift += endianByteShift) & 0xFF);
        buf[cursor++] = (byte) (l >> (shift += endianByteShift) & 0xFF);
        buf[cursor++] = (byte) (l >> (shift += endianByteShift) & 0xFF);
        buf[cursor++] = (byte) (l >> (shift += endianByteShift) & 0xFF);
        buf[cursor++] = (byte) (l >> (shift += endianByteShift) & 0xFF);

        if(cursor > len) len = cursor;
        return this;
    }

    public long readLong() {
        long result = 0;
        int shift = endianByteInitPos[LONG];

        result |= (long) (buf[cursor++] & 0xFF) << shift
         | (long) (buf[cursor++] & 0xFF) << (shift += endianByteShift)
         | (long) (buf[cursor++] & 0xFF) << (shift += endianByteShift)
         | (long) (buf[cursor++] & 0xFF) << (shift += endianByteShift)
         | (long) (buf[cursor++] & 0xFF) << (shift += endianByteShift)
         | (long) (buf[cursor++] & 0xFF) << (shift += endianByteShift)
         | (long) (buf[cursor++] & 0xFF) << (shift += endianByteShift)
         | (long) (buf[cursor++] & 0xFF) << (shift += endianByteShift);

        /*result |= (long) (buf[cursor++] & 0xFF) << shift;
        result |= (long) (buf[cursor++] & 0xFF) << (shift += endianByteShift);
        result |= (long) (buf[cursor++] & 0xFF) << (shift += endianByteShift);
        result |= (long) (buf[cursor++] & 0xFF) << (shift += endianByteShift);
        result |= (long) (buf[cursor++] & 0xFF) << (shift += endianByteShift);
        result |= (long) (buf[cursor++] & 0xFF) << (shift += endianByteShift);
        result |= (long) (buf[cursor++] & 0xFF) << (shift += endianByteShift);
        result |= (long) (buf[cursor++] & 0xFF) << (shift += endianByteShift);*/
        
        return result;
    }

    public DynBuf writeFloat(float f) {
        ensureCapacityFor(Float.BYTES);
        int shift = endianByteInitPos[FLOAT];
        int fb = Float.floatToRawIntBits(f);

        buf[cursor++] = (byte) (fb >> shift & 0xFF);
        buf[cursor++] = (byte) (fb >> (shift += endianByteShift) & 0xFF);
        buf[cursor++] = (byte) (fb >> (shift += endianByteShift) & 0xFF);
        buf[cursor++] = (byte) (fb >> (shift += endianByteShift) & 0xFF);

        if(cursor > len) len = cursor;
        return this;
    }

    public float readFloat() {
        int result = 0;
        int shift = endianByteInitPos[FLOAT];

        result |= (buf[cursor++] & 0xFF) << shift
        | (buf[cursor++] & 0xFF) << (shift += endianByteShift)
        | (buf[cursor++] & 0xFF) << (shift += endianByteShift)
        | (buf[cursor++] & 0xFF) << (shift += endianByteShift);

        return Float.intBitsToFloat(result);
    }

    public DynBuf writeDouble(double d) {
        ensureCapacityFor(Double.BYTES);
        int shift = endianByteInitPos[DOUBLE];
        long db = Double.doubleToRawLongBits(d);

        buf[cursor++] = (byte) (db >> shift & 0xFF);
        buf[cursor++] = (byte) (db >> (shift += endianByteShift) & 0xFF);
        buf[cursor++] = (byte) (db >> (shift += endianByteShift) & 0xFF);
        buf[cursor++] = (byte) (db >> (shift += endianByteShift) & 0xFF);
        buf[cursor++] = (byte) (db >> (shift += endianByteShift) & 0xFF);
        buf[cursor++] = (byte) (db >> (shift += endianByteShift) & 0xFF);
        buf[cursor++] = (byte) (db >> (shift += endianByteShift) & 0xFF);
        buf[cursor++] = (byte) (db >> (shift += endianByteShift) & 0xFF);

        if(cursor > len) len = cursor;
        return this;
    }

    public double readDouble() {
        long result = 0;
        int shift = endianByteInitPos[DOUBLE];

        result |= (long) (buf[cursor++] & 0xFF) << shift
        | (long) (buf[cursor++] & 0xFF) << (shift += endianByteShift)
        | (long) (buf[cursor++] & 0xFF) << (shift += endianByteShift)
        | (long) (buf[cursor++] & 0xFF) << (shift += endianByteShift)
        | (long) (buf[cursor++] & 0xFF) << (shift += endianByteShift)
        | (long) (buf[cursor++] & 0xFF) << (shift += endianByteShift)
        | (long) (buf[cursor++] & 0xFF) << (shift += endianByteShift)
        | (long) (buf[cursor++] & 0xFF) << (shift += endianByteShift);

        return Double.longBitsToDouble(result);
    }

    // TODO: allow for storing bytes with specific endianness?
    public DynBuf writeVarInt(int num) {
        // 0 -> (2^7 - 1)
        // 0100 0101 1011 0011
        if(num == 0) {
            writeByte((byte)0);
            return this;
        }

        int highestBit = Integer.numberOfTrailingZeros(Integer.highestOneBit(num)) + 1;
        // NOTE: the max() fn doesn't necessarily need to be used below, as it should always be 1 at the minimum
        int bytes = (int) Math.max(Math.ceil(highestBit / 7d), 1); // 7 bits of data, 1 bit for continuity
        ensureCapacityFor(bytes);

        System.out.println(highestBit);
        System.out.println(bytes);

        byte[] byteRepr = new byte[bytes];
        // TODO: reverse for-loop direction
        for(int i = bytes - 1; i >= 0; i--) {
            byte b = (byte) (num >> (7 * (bytes - 1 - i)) & 0xFF);
            if(i != 0) {
                b |= (byte) ((1 << 7) & 0xFF);
            }
            byteRepr[bytes - 1 - i] = b;
        }

        write(byteRepr);
        return this;
    }

    // TODO: allow for reading bytes with specific endianness
    public int readVarInt() {
        int result = 0;
        for(int i = 0; i < MAX_VARINT_BYTES; i++) {
            byte b = (byte) (buf[cursor++] & 0xFF);
            result |= (((b & (~(1 << 7))) & 0xFF) << (7 * i));
            if((b & (1 << 7)) == 0) {
                return result;
            }
        }
        throw new IllegalStateException("Read a malformed varint over " + MAX_VARINT_BYTES + " bytes");
    }

    // TODO: allow for custom length type (byte, short, int, long)
    public DynBuf writeUTF8(String str) {
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        ensureCapacityFor(Integer.BYTES + bytes.length);
        writeInt(bytes.length);
        return write(bytes);
    }

    public String readUTF8() {
        int len = readInt();
        byte[] bytes = read(len);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    // Prefer to use endianByteInitPos for cached initial byte for primitives
    // For types other than primitives, this will calculate the starting byte position based on endianness of the buffer
    // CONSTRAINT: bytes must be greater than 0
    @Deprecated
    private int getShift(int bytes) {
        return order == ByteOrder.LITTLE_ENDIAN ? 0 : (bytes - 1) * 8;
    }

    // Ensures that the next requested bytes can fit into the buffer, growing if necessary
    private void ensureCapacityFor(int writeBytes) {
        if(len + writeBytes > capacity()) {
            grow();
        }
    }

    // Grow by the grower
    private void grow() {
        grow(grower.applyAsInt(capacity()));
    }

    // Grow to the specified capacity
    // TODO: handle capacities smaller than the original (for shrinking); refactor to resize(int capacity) ?
    private void grow(int capacity) {
        byte[] newBuf = new byte[capacity];
        System.arraycopy(buf, 0, newBuf, 0, len);
        this.buf = newBuf;
    }

    @Override
    public String toString() {
        return "DynBuf{" +
                "buf=" + Arrays.toString(buf) +
                ", len=" + len +
                ", cursor=" + cursor +
                ", grower=" + grower +
                ", lockedRegions=" + lockedRegions +
                ", order=" + order +
                '}';
    }
}