package org.minerift.ether.database.nubin.writer;

import org.minerift.ether.database.DataType;
import org.minerift.ether.database.bin.Ref;
import org.minerift.ether.database.bin.writer.BinaryWriterContext;

import static org.minerift.ether.util.Utils.unknownIntToInt16;

public class Encoders {
    private Encoders() {}

    protected static void writeBool(WriterContext ctx, DataType<Boolean> type, boolean data) {
        ctx.currentBuffer.put((byte) (data ? 1 : 0));
    }

    protected static void writeByte(WriterContext ctx, DataType<Byte> type, byte data) {
        ctx.currentBuffer.put(data);
    }

    protected static void writeShort(WriterContext ctx, DataType<Short> type, short data) {
        ctx.currentBuffer.putShort(data);
    }

    protected static void writeInt(WriterContext ctx, DataType<Integer> type, int data) {
        ctx.currentBuffer.putInt(unknownIntToInt16(data));
    }

    protected static void writeLong(WriterContext ctx, DataType<Long> type, long data) {
        ctx.currentBuffer.putLong(data);
    }

    protected static void writeFloat(WriterContext ctx, DataType<Float> type, float data) {
        ctx.currentBuffer.putFloat(data);
    }

    protected static void writeDouble(WriterContext ctx, DataType<Double> type, double data) {
        ctx.currentBuffer.putDouble(data);
    }

    // TODO: rename to [write]NChar for clarity
    // TODO: review how strings are handled (truncate by the character, not by individual bytes)
    // TODO: !! store encoding in column info for reading/writing strategy !!
    protected static void writeChar(WriterContext ctx, DataType<String> type, String data) {
        String str;
        try {
            str = data.substring(0, type.length());
        } catch (IndexOutOfBoundsException ex) {
            str = data;
            str += " ".repeat(type.length() - str.length());
        }
        ctx.currentBuffer.put(str.getBytes());
    }

    protected static void writeVarchar(WriterContext ctx, DataType<String> type, String data) {
        ctx.currentBuffer.putShort((short) data.length());
        ctx.currentBuffer.put(data.getBytes());
    }

    protected static void writeBinary(WriterContext ctx, DataType<byte[]> type, byte[] data) {
        ctx.currentBuffer.put(data);
    }

    protected void writeVarbinary(WriterContext ctx, DataType<byte[]> type, byte[] data) {
        ctx.currentBuffer.putInt(data.length);
        ctx.currentBuffer.put(data);
    }
}
