package org.minerift.ether.test;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.minerift.ether.util.nbt.*; //TODO: review?
import org.minerift.ether.util.nbt.tags.*;
import org.minerift.ether.util.nbt.tags.container.ListTag;

import java.io.IOException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.minerift.ether.util.nbt.tags.TagTypes.*;

public class NbtTest {

    // stream of Tag's -> serialize to buf -> deserialize to Tag and test equality
    protected static Stream<Tag> tag2Buf2TagNoOptionsTest() {
        
        // NOTE: should read a few local (resource) files, add to Stream

        // TODO: create Tag's of all types (TagTypes + NuTagTypes);
        //  assert NuTagTypes aren't readable.

        // TODO: implement equals/hashcode properly for each tag type
        ByteTag btag = new ByteTag("test_byte", (byte)69);
        ShortTag stag = new ShortTag("test_short", (short)69);
        IntTag itag = new IntTag("test_int", 69);
        LongTag ltag = new LongTag("test_long", 69L);
        FloatTag ftag = new FloatTag("test_float", 69f);
        DoubleTag dtag = new DoubleTag("test_double", 69d);
        StringTag strtag = new StringTag("test_str", "Hello, world!");
        ListTag<StringTag> listtag = new ListTag<>("test_list", STRING,
            Stream.of("Hello", "world!", "Goodbye, world!", "Yoyooyoyoyoyyo42069")
                    .map(StringTag::valueOf).toList());

        return Stream.of(
            // TODO: for ScalarTag's, impl ctor with int param with check within scalar primitive range
            btag, stag, itag, ltag, ftag, dtag, strtag, listtag
        );
    }

    @ParameterizedTest
    @MethodSource
    public void tag2Buf2TagNoOptionsTest(Tag tag) throws IOException, NbtReadException {
        NbtWriter writer = NbtWriter.from();
        writer.writeTag(tag);
        NbtReader reader = NbtReader.from(writer.buf, Compression.NONE);
        Tag read = reader.readNextTag();
        assertEquals(tag, read);
    }

    // TODO: test for writing & reading multiple tags from tag stream (buf)

}