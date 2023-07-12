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
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * <p>
 * This class reads <strong>NBT</strong>, or <strong>Named Binary Tag</strong>
 * streams, and produces an object graph of subclasses of the <code>Tag</code>
 * object.
 * </p>
 *
 * <p>
 * The NBT format was created by Markus Persson, and the specification may be
 * found at <a href="http://www.minecraft.net/docs/NBT.txt">
 * http://www.minecraft.net/docs/NBT.txt</a>.
 * </p>
 *
 * @author Graham Edgecombe
 *
 */
public final class NBTInputStream implements Closeable {

    /**
     * The data input stream.
     */
    private final DataInputStream is;

    /**
     * Creates a new <code>NBTInputStream</code>, which will source its data
     * from the specified input stream.
     *
     * @param is
     *            The input stream.
     * @param gzipped
     *            Whether the stream is GZip-compressed.
     * @throws IOException
     *             if an I/O error occurs.
     */
    public NBTInputStream(InputStream is, final boolean gzipped) throws IOException {
        if (gzipped) {
            is = new GZIPInputStream(is);
        }
        this.is = new DataInputStream(is);
    }

    /**
     * Creates a new <code>NBTInputStream</code>, which will source its data
     * from the specified GZIP-compressed input stream.
     *
     * @param is
     *            The input stream.
     * @throws IOException
     *             if an I/O error occurs.
     */
    public NBTInputStream(final InputStream is) throws IOException {
        this.is = new DataInputStream(new GZIPInputStream(is));
    }

    //TODO: comment this.  supports raw Gziped data.
    // author: ensirius
    public NBTInputStream(final DataInputStream is) {
        this.is = is;
    }

    /**
     * Reads an NBT tag from the stream.
     *
     * @return The tag that was read.
     * @throws IOException
     *             if an I/O error occurs.
     */
    public Tag readTag() throws IOException {

        return readTag(0);
    }

    /**
     * Reads an NBT from the stream.
     *
     * @param depth
     *            The depth of this tag.
     * @return The tag that was read.
     * @throws IOException
     *             if an I/O error occurs.
     */
    private Tag readTag(final int depth) throws IOException {

        final int rawType = is.readByte() & 0xFF;
        final NBTTagType tagType = NBTTagType.getTagType(rawType);

        String name;
        if (tagType != NBTTagType.END_TAG) {
            final int nameLength = is.readShort() & 0xFFFF;
            final byte[] nameBytes = new byte[nameLength];
            is.readFully(nameBytes);
            name = new String(nameBytes, StandardCharsets.UTF_8);
        } else {
            name = "";
        }

        return readTagPayload(tagType, name, depth);
    }

    /**
     * Reads the payload of a tag, given the name and type.
     *
     * @param type
     *            The type.
     * @param name
     *            The name.
     * @param depth
     *            The depth.
     * @return The tag.
     * @throws IOException
     *             if an I/O error occurs.
     */
    private Tag readTagPayload(final NBTTagType type, final String name, final int depth)
            throws IOException
    {

        return switch (type) {
            case END_TAG -> {
                if (depth == 0) {
                    throw new IOException("TAG_End found without a TAG_Compound/TAG_List tag preceding it.");
                }
                yield new EndTag();
            }

            // Primitive tag types
            case BYTE_TAG -> new ByteTag(name, is.readByte());
            case SHORT_TAG -> new ShortTag(name, is.readShort());
            case INT_TAG -> new IntTag(name, is.readInt());
            case LONG_TAG -> new LongTag(name, is.readLong());
            case FLOAT_TAG -> new FloatTag(name, is.readFloat());
            case DOUBLE_TAG -> new DoubleTag(name, is.readDouble());
            case STRING_TAG -> readStringTagPayload(name);

            // Array tag types
            case BYTE_ARRAY_TAG -> readByteArrayTagPayload(name);
            case INT_ARRAY_TAG -> readIntArrayTagPayload(name);
            case LONG_ARRAY_TAG ->  readLongArrayTagPayload(name);

            // Collection tag types
            case LIST_TAG -> readListTagPayload(name, depth);
            case COMPOUND_TAG -> readCompoundTagPayload(name, depth);
        };
    }

    private ByteArrayTag readByteArrayTagPayload(String name) throws IOException {
        final int length = is.readInt();
        byte[] bytes = new byte[length];
        is.readFully(bytes);
        return new ByteArrayTag(name, bytes);
    }

    private StringTag readStringTagPayload(String name) throws IOException {
        final int length = is.readShort();
        byte[] bytes = new byte[length];
        is.readFully(bytes);
        return new StringTag(name, new String(bytes, StandardCharsets.UTF_8));
    }

    private ListTag readListTagPayload(String name, int depth) throws IOException {
        final NBTTagType childType = NBTTagType.getTagType(is.readByte());
        if(childType == NBTTagType.END_TAG) {
            throw new IOException("TAG_End not permitted as child type for TAG_List");
        }

        final int length = is.readInt();
        final List<Tag> tagList = new ArrayList<>(length);
        for(int i = 0; i < length; i++) {
            final Tag tag = readTagPayload(childType, "", depth + 1);
            tagList.add(tag);
        }

        return new ListTag(name, childType, tagList);
    }

    private CompoundTag readCompoundTagPayload(String name, int depth) throws IOException {
        final Map<String, Tag> tags = new HashMap<>();
        while(true) {
            final Tag tag = readTag(depth + 1);
            if(tag instanceof EndTag) {
                break;
            }
            tags.put(tag.getName(), tag);
        }

        return new CompoundTag(name, tags);
    }

    private IntArrayTag readIntArrayTagPayload(String name) throws IOException {
        final int length = is.readInt();
        int[] ints = new int[length];
        for(int i = 0; i < length; i++) {
            ints[i] = is.readInt();
        }
        return new IntArrayTag(name, ints);
    }

    private LongArrayTag readLongArrayTagPayload(String name) throws IOException {
        final int length = is.readInt();
        long[] longs = new long[length];
        for(int i = 0; i < length; i++) {
            longs[i] = is.readLong();
        }
        return new LongArrayTag(name, longs);
    }

    @Override
    public void close() throws IOException {

        is.close();
    }
}