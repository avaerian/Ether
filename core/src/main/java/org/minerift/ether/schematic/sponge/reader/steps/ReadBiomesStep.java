package org.minerift.ether.schematic.sponge.reader.steps;

import org.minerift.ether.debug.NeedsTesting;
import org.minerift.ether.nms.BiomeNotFoundException;
import org.minerift.ether.nms.world.Biome;
import org.minerift.ether.schematic.SchematicFileReadException;
import org.minerift.ether.schematic.data.Array3DOrder;
import org.minerift.ether.schematic.data.BiomeVolume;
import org.minerift.ether.schematic.data.BytePalette;
import org.minerift.ether.schematic.sponge.reader.SchematicReaderContext;
import org.minerift.ether.util.nbt.tags.IntTag;
import org.minerift.ether.util.nbt.tags.Tag;
import org.minerift.ether.util.nbt.tags.container.CompoundTag;
import org.minerift.ether.util.nbt.tags.container.MismatchedTypeException;
import org.minerift.ether.util.nbt.tags.container.NoTagFoundException;

import java.util.Map;

import static java.lang.String.format;
import static org.minerift.ether.schematic.sponge.reader.SchematicNBTFields.*;
import static org.minerift.ether.util.nbt.tags.TagTypes.INT;

public class ReadBiomesStep implements IReaderStep {

    @NeedsTesting
    @Override
    public void read(SchematicReaderContext ctx) throws SchematicFileReadException {

        final int width = ctx.builder.getWidth();
        final int height = ctx.builder.getHeight();
        final int length = ctx.builder.getLength();

        BiomeVolume.Builder biomeVolume = BiomeVolume.builder()
                .setOrder(Array3DOrder.YZX)
                .setDimensions(width, height, length);

        final CompoundTag tagList;
        try {
            tagList = ctx.root.getCompound(NBT_BIOME_PALETTE);
        } catch(NoTagFoundException | MismatchedTypeException e) {
            BytePalette<String> biomePalette = BytePalette.of(1);
            biomePalette.add((byte) 0, "minecraft:air"); // TODO: review
            biomeVolume.setData(new byte[width * height * length]); // TODO: fix this with EmptyBiomeVolume

            ctx.builder.setBiomes(biomeVolume);
            return;
        }

        // Read biome palette
        Map<String, Tag> biomePaletteRaw = tagList.getValue();
        BytePalette<Biome<?>> biomePalette = BytePalette.of(biomePaletteRaw.size());

        // Verify size
        IntTag _expSize = ctx.root.tryGetTag(NBT_BIOME_PALETTE_MAX, INT);
        if(_expSize != null) {
            int expSize = _expSize.getAsInt(); // optional
            if(biomePaletteRaw.size() != expSize) {
                // TODO: proper logger
                System.out.printf("Expected a palette size of %d, but actually got %d%n", expSize, biomePaletteRaw.size());
            }
        }

        // Map from raw palette to actual palette
        biomePaletteRaw.forEach((biomeId, idx) -> {
            Biome<?> biome;
            try {
                biome = Biome.of(biomeId);
            } catch (BiomeNotFoundException ex) {
                // TODO: for invalid biomes, either throw, ignore (no biome), or set to fallback
                biome = null; // nulls will be ignored
            }
            biomePalette.add((byte)((IntTag)idx).getAsInt(), biome);
        });

        // Prepare to read data
        final byte[] biomesRaw = ctx.root.getByteArray(NBT_BIOME_DATA,
                (e) -> new SchematicFileReadException("Failed to read biome data", e));

        System.out.println(
                "biomesRaw size: " + biomesRaw.length + ", total blocks: " + (width * height * length)); // debug

        biomeVolume.setData(biomesRaw);
        biomeVolume.setPalette(biomePalette);

        ctx.builder.setBiomes(biomeVolume);
    }

}
