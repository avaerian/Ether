package org.minerift.ether.util.nbt;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.minerift.ether.debug.NeedsTesting;
import org.minerift.ether.util.Predicates;
import org.minerift.ether.util.nbt.tags.*;
import org.minerift.ether.util.nbt.tags.container.NoTagTypeFoundException;

import java.io.*;
import java.nio.channels.Channels;
import java.util.function.Predicate;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

import static org.minerift.ether.util.nbt.tags.TagTypes.END;
import static org.minerift.ether.util.nbt.tags.TagTypes.lookup;

public class NbtReader extends NbtTraverser {

    public static NbtReader from(File f, NbtOption... options) throws IOException {
        try(RandomAccessFile raf = new RandomAccessFile(f, "r")) {
            short magic = raf.readShort(); // big-endian; possible magic
            raf.seek(raf.getFilePointer() - Short.BYTES);
            return from(raf, Compression.fromMagic(magic), Predicates.always(), options);
        }
    }

    public static NbtReader from(File f, Compression compress, NbtOption... options) throws IOException {
        try(RandomAccessFile raf = new RandomAccessFile(f, "r")) {
            return from(raf, compress, Predicates.always(), options);
        }
    }

    /* There's quite a bit of duplication below; this is perfectly fine for the
    purpose of providing clarity and seeing what's actually happening */

    public static NbtReader.Builder withOptions(File f) throws IOException {
        RandomAccessFile raf = new RandomAccessFile(f, "r");
        return withOptions(raf, NO_OPTIONS);
    }

    public static NbtReader.Builder withOptions(File f, NbtOption... options) throws IOException {
        RandomAccessFile raf = new RandomAccessFile(f, "r");
        return withOptions(raf, options);
    }

    public static NbtReader.Builder withOptions(File f, @NotNull Compression compress) throws IOException {
        RandomAccessFile raf = new RandomAccessFile(f, "r");
        InputStream input = Channels.newInputStream(raf.getChannel());
        int len = Math.toIntExact(raf.length()); // FIXME: fix rare edge case (would need to do a buffered read)
        return new Builder(input, raf, compress, len, NO_OPTIONS);
    }

    public static NbtReader.Builder withOptions(File f, @NotNull Compression compress, NbtOption... options) throws IOException {
        RandomAccessFile raf = new RandomAccessFile(f, "r");
        InputStream input = Channels.newInputStream(raf.getChannel());
        int len = Math.toIntExact(raf.length());
        return new Builder(input, raf, compress, len, options);
    }

    // don't close RandomAccessFile
    public static NbtReader.Builder withOptions(RandomAccessFile raf) throws IOException {
        short magic = raf.readShort();
        raf.seek(raf.getFilePointer() - Short.BYTES);
        Compression compress = Compression.fromMagic(magic);
        InputStream input = Channels.newInputStream(raf.getChannel());
        int len = Math.toIntExact(raf.length());
        return new Builder(input, null, compress, len, NO_OPTIONS);
    }

    // don't close RandomAccessFile
    public static NbtReader.Builder withOptions(RandomAccessFile raf, NbtOption... options) throws IOException {
        short magic = raf.readShort();
        raf.seek(raf.getFilePointer() - Short.BYTES);
        Compression compress = Compression.fromMagic(magic);
        InputStream input = Channels.newInputStream(raf.getChannel());
        int len = Math.toIntExact(raf.length());
        return new Builder(input, null, compress, len, options);
    }

    // don't close RandomAccessFile
    public static NbtReader.Builder withOptions(RandomAccessFile raf, @NotNull Compression compress, NbtOption... options) throws IOException {
        InputStream input = Channels.newInputStream(raf.getChannel());
        int len = Math.toIntExact(raf.length());
        return new Builder(input, null, compress, len, options);
    }

    // Doesn't close the RandomAccessFile
    // NOTE: it seems GZipInputStream (GZIPInputStream.GZIP_MAGIC specifically)
    // uses little-endian (Intel) byte ordering, so keep that in-mind.
    @NeedsTesting
    public static NbtReader from(@NotNull RandomAccessFile raf, @NotNull Compression compress, @NotNull Predicate<TagHeader> tagSelector, NbtOption... options) throws IOException {
        InputStream input = Channels.newInputStream(raf.getChannel());
        int len = Math.toIntExact(raf.length());
        ByteBuf buf = decompress(input, compress, null, len);
        return new NbtReader(buf, tagSelector, options);
    }

    // len is the actual length of the uncompressed data; this may grow during decompression, obviously
    protected static ByteBuf decompress(@NotNull InputStream input, @NotNull Compression compress, @Nullable ByteBuf dst, int len) throws IOException {
        switch (compress) {
            case NONE -> {
                if(dst == null) {
                    dst = Unpooled.buffer(len);
                }
                dst.writeBytes(input, dst.capacity());
            }
            case GZIP -> {
                InputStream is = new GZIPInputStream(input);
                if(dst == null) {
                    dst = Unpooled.wrappedBuffer(is.readAllBytes());
                } else {
                    dst.writeBytes(is.readAllBytes());
                }
            }
            case ZLIB -> { // needs testing
                InputStream is = new InflaterInputStream(input);
                if(dst == null) {
                    dst = Unpooled.wrappedBuffer(is.readAllBytes());
                } else {
                    dst.writeBytes(is.readAllBytes());
                }
            }
            default -> throw new IllegalStateException("Unexpected value: " + compress);
        }
        return dst;
    }

    public static NbtReader from(ByteBuf buf, NbtOption ... options) throws IOException {
        return from(buf, null, Predicates.always(), options);
    }

    public static NbtReader from(ByteBuf buf, Compression compress, NbtOption ... options) throws IOException {
        return from(buf, compress, Predicates.always(), options);
    }

    public static NbtReader from(@NotNull ByteBuf buf, @Nullable Compression compress, @Nullable Predicate<TagHeader> tagSelector, NbtOption ... options) throws IOException {
        if(compress == null) { // tentatively find compression type
            int wpos = buf.writerIndex();
            short magic = buf.readShort();
            buf.writerIndex(wpos);

            compress = Compression.fromMagic(magic);
        }
        if(tagSelector == null) {
            tagSelector = Predicates.always();
        }

        ByteBuf _buf;
        try(InputStream input = new ByteBufInputStream(buf)) {
            _buf = decompress(input, compress, buf.copy().resetReaderIndex().resetWriterIndex(), buf.readableBytes());
        }
        return new NbtReader(_buf, tagSelector, options);
    }

    public NbtReader(ByteBuf buf, /*boolean bigEndian,*/ Predicate<TagHeader> tagSelector, NbtOption ... options) {
        super(buf, tagSelector, options);
    }

    // throws NoTagTypeFoundException if id read from buffer isn't recognized
    public <T extends Tag> T readNextTag(TagType<T> expected)
            throws IllegalStateException, NbtReadException, NoTagTypeFoundException {
        byte typeId = readByte();
        TagType<?> type = TagTypes.lookupOrThrow(typeId);
        if(type != expected) { // no other refs than the static ones should exist
            throw new IllegalStateException("Expected tag type " + expected + ", found " + type);
        }
        String name = readUTF8();
        return expected.codec().readTag(this, name);
    }

    public Tag readNextTag() throws NbtReadException {
        byte typeId = readByte();
        TagType<?> type = lookup(typeId);
        if(type == END) { // weird edge-case; review if can be removed
            return EndTag.INST;
        }
        String name = readUTF8();
        return type.codec().readTag(this, name);
    }

    public static class Builder {

        protected final Closeable inputToClose;
        protected final InputStream input;
        protected int len; // len of uncompressed data
        protected Compression compress;
        protected ByteBuf buf; // buffer NbtReader will use
        protected Predicate<TagHeader> tagSelector;
        protected NbtOption[] options;

        protected Builder(@NotNull InputStream input, @Nullable Closeable inputToClose,
                          @NotNull Compression compress, int len,
                          @NotNull NbtOption[] options) {
            this.input = input;
            this.inputToClose = inputToClose;
            this.compress = compress;
            this.len = len;
            this.buf = null;
            this.tagSelector = Predicates.always();
            this.options = options;
        }

        public InputStream input() {
            return input;
        }

        public NbtOption[] options() {
            return options;
        }

        public Builder options(NbtOption ... options) {
            this.options = options;
            return this;
        }

        public ByteBuf buffer() {
            return buf;
        }

        public Builder buffer(ByteBuf buf) {
            this.buf = buf;
            return this;
        }

        // len of uncompressed data
        public int len() {
            return len;
        }

        // len of uncompressed data
        public Builder len(int len) {
            this.len = len;
            return this;
        }

        public Predicate<TagHeader> tagSelector() {
            return tagSelector;
        }

        public Builder tagSelector(Predicate<TagHeader> tagSelector) {
            this.tagSelector = tagSelector;
            return this;
        }

        public NbtReader build() throws IOException {
            // buf may be null, so return used or new buf
            ByteBuf _buf = decompress(input, compress, buf, len);

            try {
                return new NbtReader(_buf, tagSelector, options);
            } finally {
                if(inputToClose != null) {
                    inputToClose.close();
                }
            }
        }

    }

}
