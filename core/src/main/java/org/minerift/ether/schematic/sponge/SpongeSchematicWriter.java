package org.minerift.ether.schematic.sponge;

import org.minerift.ether.util.Predicates;
import org.minerift.ether.util.nbt.NbtWriter;
import org.minerift.ether.util.nbt.tags.IntTag;
import org.minerift.ether.util.nbt.tags.ShortTag;
import org.minerift.ether.util.nbt.tags.array.ByteArrayTag;
import org.minerift.ether.util.nbt.tags.container.CompoundTag;

import java.io.File;
import java.nio.ByteBuffer;

import static org.minerift.ether.schematic.sponge.reader.SchematicNBTFields.*;

public class SpongeSchematicWriter {

    public void write(SpongeSchematic schem, File file) {

        CompoundTag root = new CompoundTag();

        root.addTag(new ShortTag(NBT_WIDTH, (short) schem.getWidth()));
        root.addTag(new ShortTag(NBT_HEIGHT, (short) schem.getHeight()));
        root.addTag(new ShortTag(NBT_LENGTH, (short) schem.getLength()));

        root.addTag(new IntTag(NBT_VERSION, 3)); // SpongeVersion.V3

        CompoundTag biomePalette = new CompoundTag(NBT_BIOME_PALETTE);


        //root.addTag();
        root.addTag(new ByteArrayTag(NBT_BLOCK_DATA, schem.getBlocks().getData()));

        root.addTag(new ByteArrayTag(NBT_BIOME_DATA, schem.getBiomes().getData()));

        ByteBuffer buffer = ByteBuffer.allocate(4096); // FIXME: use DynBuf instead of this
        NbtWriter writer = new NbtWriter(buffer, true, Predicates.always());
        writer.writeTag(root);

    }

}
