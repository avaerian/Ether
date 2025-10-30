package org.minerift.ether.test;

import io.netty.buffer.Unpooled;
import io.netty.buffer.ByteBuf;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.minerift.ether.util.nbt.*; //TODO: review?

import static org.minerift.ether.util.nbt.tags.TagTypes.*;

public class NbtTest {

    // stream of Tag's -> serialize to buf -> deserialize to Tag and test equality
    protected Stream<Tag> onReadNoOptionsTest() {
        
        // TODO: read a few local (resource) files, add to Stream

        // TODO: create Tag's of all types (TagTypes + NuTagTypes)
        //  Assert NuTagTypes aren't readable

        ByteTag btag = new ByteTag("test_byte", (byte)69);
        ShortTag stag = new ShortTag("test_short", (short)69);
        IntTag itag = new IntTag("test_int", 69);
        LongTag ltag = new LongTag("test_long", 69L); // TODO: review long num notation
        FloatTag ftag = new FloatTag("test_float", 69f);
        DoubleTag dtag = new DoubleTag("test_double", 69d);
        StringTag strtag = new StringTag("test_str", "Hello, world!");
        ListTag<StringTag> listtag = new ListTag("test_list", STRING, 
            List.of("Hello", "world!, "Goodbye, world!" "Yoyooyoyoyoyyo42069")); //FIXME: create WrapTags tag transmuter

        Stream.of(
            // TODO: for ScalarTag's, impl ctor with int param with check within scalar primitive range
            btag, stag, itag, ltag, ftag, dtag, strtag, listtag
        );
    }

    @ParameterizedTest
    @MethodSource
    public void tag2Buf2TagNoOptionsTest(Tag tag) {
        NbtWriter writer = NbtWriter.from(Compression.NONE);
        ByteBuf buf = writer.writeTag(tag);
        NbtReader reader = NbtReader.from(buf, Compression.NONE);
        Tag read = reader.readNextTag(); // TODO: add expected tag type?
        assertEquals(tag, read);
    }

    // TODO: test for writing & reading multiple tags from tag stream (buf)

}