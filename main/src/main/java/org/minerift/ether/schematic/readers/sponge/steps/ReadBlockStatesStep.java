package org.minerift.ether.schematic.readers.sponge.steps;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.minerift.ether.schematic.readers.sponge.ReaderContext;
import org.minerift.ether.schematic.SchematicFileReadException;
import org.minerift.ether.schematic.types.SpongeSchematic;
import org.minerift.ether.math.Vec3i;
import org.minerift.ether.util.nbt.NBTSectionView;
import org.minerift.ether.util.nbt.tags.IntTag;
import org.minerift.ether.util.nbt.tags.Tag;
import org.minerift.ether.world.BlockArchetype;
import org.minerift.ether.world.BlockEntityArchetype;

import java.util.ArrayList;
import java.util.Map;

import static org.minerift.ether.schematic.readers.sponge.SchematicNBTFields.*;

public class ReadBlockStatesStep implements IReaderStep {

    @Override
    public void read(ReaderContext ctx) throws SchematicFileReadException {

        final SpongeSchematic.Builder builder = ctx.builder;
        final NBTSectionView rootView = ctx.rootView;

        final short width = builder.getWidth();
        final short height = builder.getHeight();
        final short length = builder.getLength();

        // Prepare to parse palette
        Int2ObjectMap<String> palette = new Int2ObjectOpenHashMap<>();
        Map<String, Tag> paletteRaw = rootView.getSectionView(NBT_PALETTE)
                .orElseThrow(() -> new SchematicFileReadException("Failed to read block state palette!"))
                .getSectionTags();

        // Verify palette size
        rootView.getInt(NBT_PALETTE_MAX).ifPresent((expected) -> {
            if(paletteRaw.size() != expected) {
                // TODO: proper logger
                System.out.println(String.format("Expected a palette size of %d, but actually got %d", expected, paletteRaw.size()));
            }
        });

        // Map raw palette to actual palette
        paletteRaw.forEach((rawData, idx) -> palette.put(((IntTag)idx).getValue().intValue(), rawData));

        // Prepare to read block data
        byte[] blockDataRaw = rootView.getByteArray(NBT_BLOCK_DATA).orElseThrow(() -> new SchematicFileReadException("Failed to read block data!"));
        builder.setBlocks(new ArrayList<>(width * height * length));

        final Vec3i.Mutable mutablePos = new Vec3i.Mutable(0, 0, 0);
        final Map<Vec3i, BlockEntityArchetype> blockEntities = builder.getBlockEntities();

        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                for(int z = 0; z < length; z++) {

                    mutablePos.set(x, y, z);

                    BlockEntityArchetype blockEntity = blockEntities.get(mutablePos);
                    if(blockEntity != null) {
                        builder.getBlocks().add(blockEntity);
                    } else {
                        final int index = x + (z * width) + (y * width * length);
                        final int blockId = blockDataRaw[index] & 0xFF;
                        builder.getBlocks().add(new BlockArchetype(palette.get(blockId), mutablePos.newImmutable()));
                    }
                }
            }
        }

    }
}
