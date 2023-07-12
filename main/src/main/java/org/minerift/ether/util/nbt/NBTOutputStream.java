package org.minerift.ether.util.nbt;

/*
 * JNBT License
 *
 * Copyright (c) 2010 Graham Edgecombe
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *
 *     * Neither the name of the JNBT team nor the names of its
 *       contributors may be used to endorse or promote products derived from
 *       this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

import org.minerift.ether.util.nbt.tags.*;

import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.GZIPOutputStream;


/**
 * <p>
 * This class writes <strong>NBT</strong>, or <strong>Named Binary Tag</strong>
 * <code>Tag</code> objects to an underlying <code>OutputStream</code>.
 * </p>
 *
 * <p>
 * The NBT format was created by Markus Persson, and the specification may be
 * found at <a href="http://www.minecraft.net/docs/NBT.txt">
 * http://www.minecraft.net/docs/NBT.txt</a>.
 * </p>
 *
 * @author Graham Edgecombe, Jocopa3
 *
 */
public final class NBTOutputStream implements Closeable {

    /**
     * The output stream.
     */
    private final DataOutputStream os;

    /**
     * Creates a new <code>NBTOutputStream</code>, which will write data to the
     * specified underlying output stream, GZip-compressed.
     *
     * @param os
     *            The output stream.
     * @throws IOException
     *             if an I/O error occurs.
     */
    public NBTOutputStream(final OutputStream os) throws IOException {

        this.os = new DataOutputStream(new GZIPOutputStream(os));
    }


    /**
     * Creates a new <code>NBTOutputStream</code>, which will write data to the
     * specified underlying output stream.
     *
     * @param os
     *            The output stream.
     * @param gzipped
     *            Whether the output stream should be GZip-compressed.
     * @throws IOException
     *             if an I/O error occurs.
     */
    public NBTOutputStream(OutputStream os, final boolean gzipped) throws IOException {
        if (gzipped) {
            os = new GZIPOutputStream(os);
        }
        this.os = new DataOutputStream(os);
    }

    /**
     * Writes a tag.
     *
     * @param tag
     *            The tag to write.
     * @throws IOException
     *             if an I/O error occurs.
     */
    public void writeTag(final Tag tag) throws IOException {

        final NBTTagType type = tag.getTagType();
        final String name = tag.getName();
        final byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);

        os.writeByte(type.getId());
        os.writeShort(nameBytes.length);
        os.write(nameBytes);

        if (type == NBTTagType.END_TAG) { throw new IOException(
                "[JNBT] Named TAG_End not permitted."); }

        writeTagPayload(tag);
    }

    /**
     * Writes tag payload.
     *
     * @param tag
     *            The tag.
     * @throws IOException
     *             if an I/O error occurs.
     */
    private void writeTagPayload(final Tag tag) throws IOException {
        switch (tag.getTagType()) {
            case END_TAG        -> writeEndTagPayload((EndTag) tag);
            case BYTE_TAG       -> writeByteTagPayload((ByteTag) tag);
            case INT_TAG        -> writeIntTagPayload((IntTag) tag);
            case LIST_TAG       -> writeListTagPayload((ListTag) tag);
            case LONG_TAG       -> writeLongTagPayload((LongTag) tag);
            case FLOAT_TAG      -> writeFloatTagPayload((FloatTag) tag);
            case SHORT_TAG      -> writeShortTagPayload((ShortTag) tag);
            case DOUBLE_TAG     -> writeDoubleTagPayload((DoubleTag) tag);
            case STRING_TAG     -> writeStringTagPayload((StringTag) tag);
            case COMPOUND_TAG   -> writeCompoundTagPayload((CompoundTag) tag);
            case INT_ARRAY_TAG  -> writeIntArrayTagPayload((IntArrayTag) tag);
            case BYTE_ARRAY_TAG -> writeByteArrayTagPayload((ByteArrayTag) tag);
            case LONG_ARRAY_TAG -> writeLongArrayTagPayload((LongArrayTag) tag);
        }
    }

    /**
     * Writes a <code>TAG_Byte</code> tag.
     *
     * @param tag
     *            The tag.
     * @throws IOException
     *             if an I/O error occurs.
     */
    private void writeByteTagPayload(final ByteTag tag) throws IOException {

        os.writeByte(tag.getValue());
    }

    /**
     * Writes a <code>TAG_Byte_Array</code> tag.
     *
     * @param tag
     *            The tag.
     * @throws IOException
     *             if an I/O error occurs.
     */
    private void writeByteArrayTagPayload(final ByteArrayTag tag) throws IOException {

        final byte[] bytes = tag.getValue();
        os.writeInt(bytes.length);
        os.write(bytes);
    }

    /**
     * Writes a <code>TAG_Compound</code> tag.
     *
     * @param tag
     *            The tag.
     * @throws IOException
     *             if an I/O error occurs.
     */
    private void writeCompoundTagPayload(final CompoundTag tag) throws IOException {

        for (final Tag childTag : tag.getValue().values()) {
            writeTag(childTag);
        }
        os.writeByte((byte) 0); // end tag - better way?
    }

    /**
     * Writes a <code>TAG_List</code> tag.
     *
     * @param tag
     *            The tag.
     * @throws IOException
     *             if an I/O error occurs.
     */
    private void writeListTagPayload(final ListTag tag) throws IOException {

        final List<Tag> tags = tag.getValue();
        final int size = tags.size();

        os.writeByte(tag.getTagType().getId());
        os.writeInt(size);
        for (int i = 0; i < size; i++) {
            writeTagPayload(tags.get(i));
        }
    }

    /**
     * Writes a <code>TAG_String</code> tag.
     *
     * @param tag
     *            The tag.
     * @throws IOException
     *             if an I/O error occurs.
     */
    private void writeStringTagPayload(final StringTag tag) throws IOException {
        final byte[] bytes = tag.getValue().getBytes(StandardCharsets.UTF_8);
        os.writeShort(bytes.length);
        os.write(bytes);
    }

    /**
     * Writes a <code>TAG_Double</code> tag.
     *
     * @param tag
     *            The tag.
     * @throws IOException
     *             if an I/O error occurs.
     */
    private void writeDoubleTagPayload(final DoubleTag tag) throws IOException {
        os.writeDouble(tag.getValue());
    }

    /**
     * Writes a <code>TAG_Float</code> tag.
     *
     * @param tag
     *            The tag.
     * @throws IOException
     *             if an I/O error occurs.
     */
    private void writeFloatTagPayload(final FloatTag tag) throws IOException {
        os.writeFloat(tag.getValue());
    }

    /**
     * Writes a <code>TAG_Long</code> tag.
     *
     * @param tag
     *            The tag.
     * @throws IOException
     *             if an I/O error occurs.
     */
    private void writeLongTagPayload(final LongTag tag) throws IOException {
        os.writeLong(tag.getValue());
    }

    /**
     * Writes a <code>TAG_Int</code> tag.
     *
     * @param tag
     *            The tag.
     * @throws IOException
     *             if an I/O error occurs.
     */
    private void writeIntTagPayload(final IntTag tag) throws IOException {
        os.writeInt(tag.getValue());
    }

    /**
     * Writes a <code>TAG_Short</code> tag.
     *
     * @param tag
     *            The tag.
     * @throws IOException
     *             if an I/O error occurs.
     */
    private void writeShortTagPayload(final ShortTag tag) throws IOException {
        os.writeShort(tag.getValue());
    }

    /**
     * Writes a <code>TAG_Int_Array</code> tag.
     *
     * @param tag
     *            The tag.
     * @throws IOException
     *             if an I/O error occurs.
     */
    private void writeIntArrayTagPayload(final IntArrayTag tag) throws IOException {
        final int[] ints = tag.getValue();
        os.writeInt(ints.length);
        for (int i = 0; i < ints.length; i++) {
            os.writeInt(ints[i]);
        }
    }

    /**
     * Writes a <code>TAG_Long_Array</code> tag.
     *
     * @param tag
     *            The tag.
     * @throws IOException
     *             if an I/O error occurs.
     */
    private void writeLongArrayTagPayload(final LongArrayTag tag) throws IOException {

        final long[] longs = tag.getValue();
        os.writeLong(longs.length);
        for (int i = 0; i < longs.length; i++) {
            os.writeLong(longs[i]);
        }
    }

    /**
     * Writes a <code>TAG_Empty</code> tag.
     *
     * @param tag
     *            The tag.
     * @throws IOException
     *             if an I/O error occurs.
     */
    private void writeEndTagPayload(final EndTag tag) {

        /* empty */
    }

    @Override
    public void close() throws IOException {

        os.close();
    }
}