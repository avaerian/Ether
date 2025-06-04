package org.minerift.ether.util.nunbt;

import org.minerift.ether.Secrets;
import org.minerift.ether.debug.Debug;
import org.minerift.ether.util.nunbt.tags.container.CompoundTag;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.zip.GZIPOutputStream;

@Debug
public class NuNBTPlayground {

    @Debug
    public static void main(String[] args) throws IOException {

        final File file = new File(Secrets.LOCAL_WORLD_FILE_LOC);

        NbtReader reader = NbtReader.from(file, Compression.GZIP);
        try {
            System.out.println(Arrays.toString(reader.buffer.array()));
            CompoundTag tag = (CompoundTag) reader.readNextTag();
            System.out.println(tag);

            ByteBuffer writerBuf = ByteBuffer.allocate(4096);
            NbtWriter writer = new NbtWriter(writerBuf, true);
            writer.writeTag(tag);
            System.out.println(Arrays.toString(writer.buffer.array()));

            reader = new NbtReader(writerBuf, true);
            reader.buffer.flip();
            CompoundTag tag2 = (CompoundTag) reader.readNextTag();
            System.out.println(tag2);
        } finally {
            System.out.println(reader);
        }


    }
}
