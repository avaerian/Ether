package org.minerift.ether.util.nbt;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import org.minerift.ether.util.Predicates;
import org.minerift.ether.util.nbt.tags.Tag;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.function.Predicate;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;

import static org.minerift.ether.debug.NeedsTesting;
import static org.minerift.ether.debug.Experimental;

public class NbtWriter extends NbtTraverser {

    public static NbtWriter.Builder withOptions() {
        return new Builder();
    }

    // mirror of ctor for API uniformity
    @Experimental
    @NeedsTesting
    public static NbtWriter from(ByteBuf buf, NbtOption... options) {
        return new NbtWriter(buf, Predicates.always(), options);
    }

    /**
     * NOTE: when supplying an existing buf, ensure
     * writer/reader indexes are handled appropriately
     */
    public static NbtWriter from(ByteBuf buf) {
        return new NbtWriter(buf, Predicates.always(), NO_OPTIONS);
    }

    public static NbtWriter from() {
        return from(Unpooled.buffer(1024));
    }

    // tag selector has no function for writing
    public NbtWriter(ByteBuf buf/*,boolean bigEndian*/, Predicate<TagHeader> tagSelector, NbtOption... options) {
        super(buf, tagSelector, options);
    }

    // Writes compressed data to provided buffer from current buffer, then
    // copies over to current buffer
    public NbtWriter compress(ByteBuf newBuf, Compression compress) throws IOException {
        int wbegin = newBuf.writerIndex();
        compressTo(newBuf, compress);
        buf.writeBytes(newBuf, wbegin, newBuf.readableBytes()); // copy compressed data from newBuf to buf
        return this;
    }

    // Writes compressed data to provided buffer from current buffer,
    // leaving the current buffer unchanged
    // NOTE: ensure reader and writer index markers are appropriate
    public void compressTo(ByteBuf newBuf, Compression compress) throws IOException {
        // gzip and zlib have almost identical impls; I'm doing this to keep everything
        // explicit and easily updateable if impls change for some reason
        switch (compress) {
            case GZIP -> {
                // no need to zero new buf; read idx -> write idx is readable bytes region; junk data won't be included
                newBuf.resetReaderIndex();
                newBuf.resetWriterIndex();

                OutputStream out = new GZIPOutputStream(new ByteBufOutputStream(newBuf));
                buf.readBytes(out, buf.readableBytes()); // write compressed data to newBuf (OutputStream)
            }
            case ZLIB -> {
                newBuf.resetReaderIndex();
                newBuf.resetWriterIndex();
                //int wbegin = newBuf.writerIndex();

                OutputStream out = new DeflaterOutputStream(new ByteBufOutputStream(newBuf));
                buf.readBytes(out, buf.readableBytes());
            }
        }
    }

    public NbtWriter compress(Compression compress) throws IOException {
        return compress(buf.copy(), compress);
    }

    public int writeTag(Tag tag) {
        int begin = buf.writerIndex();
        writeByte(tag.type().getId());
        writeUTF8(tag.getName());
        ((TagCodec<Tag>)tag.type().codec()).writeTag(this, tag);
        return buf.writerIndex() - begin;
    }

    public int dump(File file) throws IOException {
        try(RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            return dump(raf);
        }
    }

    /**
     * NOTE: does not close RandomAccessFile; ensure this is handled properly
     * @param file file with IO capabilities
     * @return bytes written
     * @throws IOException
     */
    public int dump(RandomAccessFile file) throws IOException {
        return dump(file.getChannel());
    }

    public int dump(OutputStream out) throws IOException {
        int bytes = buf.readableBytes();
        buf.readBytes(out, bytes);
        return bytes;
    }

    public int dump(FileChannel out) throws IOException {
        int bytes = buf.readBytes(out, 0, buf.readableBytes());
        out.position(bytes);
        return bytes;
    }

    public static class Builder {

        protected ByteBuf buf;
        protected Predicate<TagHeader> tagSelector;
        protected NbtOption[] options;

        public Builder() {
            this.buf = null;
            this.tagSelector = Predicates.always();
            this.options = NO_OPTIONS;
        }

        public ByteBuf buffer() {
            return buf;
        }

        public Builder buffer(ByteBuf buf) {
            this.buf = buf;
            return this;
        }

        // no use for tagSelector -> may support in future?
        @Deprecated
        public Predicate<TagHeader> allowedTags() {
            return tagSelector;
        }

        @Deprecated
        public Builder allowedTags(Predicate<TagHeader> tagSelector) {
            this.tagSelector = tagSelector;
            return this;
        }

        public NbtOption[] options() {
            return options;
        }

        public Builder options(NbtOption ... options) {
            this.options = options;
            return this;
        }

        public NbtWriter build() {
            if(buf == null) {
                buf = Unpooled.buffer(1024); // default 1kb buf
            }
            return new NbtWriter(buf, tagSelector, options);
        }

    }
}
