package org.minerift.ether.schematic.sponge.reader.steps;

import org.minerift.ether.debug.NeedsTesting;
import org.minerift.ether.nms.BiomeNotFoundException;
import org.minerift.ether.nms.world.Biome;
import org.minerift.ether.schematic.SchematicReadException;
import org.minerift.ether.schematic.data.Array3DOrder;
import org.minerift.ether.schematic.data.BiomeVolume;
import org.minerift.ether.schematic.data.BytePalette;
import org.minerift.ether.schematic.sponge.reader.SchematicReaderContext;
import org.minerift.ether.util.nbt.tags.IntTag;
import org.minerift.ether.util.nbt.tags.container.CompoundTag;
import org.minerift.ether.util.nbt.tags.container.MismatchedTypeException;
import org.minerift.ether.util.nbt.tags.container.NoTagFoundException;

import static org.minerift.ether.schematic.sponge.reader.SchematicNBTFields.*;
import static org.minerift.ether.util.nbt.tags.TagTypes.INT;

public class ReadBiomesStep implements IReaderStep {

    @NeedsTesting
    @Override
    public void read(SchematicReaderContext ctx) throws SchematicReadException {
        final int width = ctx.builder.getWidth();
        final int height = ctx.builder.getHeight();
        final int length = ctx.builder.getLength();

        BiomeVolume.Builder biomeVolume = BiomeVolume.builder()
                .setOrder(Array3DOrder.YZX)
                .setDimensions(width, height, length);

        final CompoundTag biomePaletteTag;
        try {
            biomePaletteTag = ctx.root.getCompound(NBT_BIOME_PALETTE);
        } catch(NoTagFoundException | MismatchedTypeException e) {
            BytePalette<String> biomePalette = BytePalette.of(1);
            biomePalette.add((byte) 0, "minecraft:plains");
            biomeVolume.setData(new byte[width * height * length]);

            ctx.builder.setBiomes(biomeVolume);
            return;
        }

        // Read biome palette
        //Map<String, Tag> biomePaletteTag = biomePaletteTag.getValue();
        BytePalette<Biome<?>> biomePalette = BytePalette.of(biomePaletteTag.size());

        // Verify size
        IntTag _expSize = ctx.root.tryGetTag(NBT_BIOME_PALETTE_MAX, INT); // optional
        if(_expSize != null) {
            int expSize = _expSize.getAsInt();
            if(biomePaletteTag.size() != expSize) {
                // TODO: proper logger
                System.out.printf("Expected a palette size of %d, but actually got %d\n",
                        expSize, biomePaletteTag.size());
            }
        }

        // Map from raw palette to actual palette
        biomePaletteTag.forEach((biomeId, tag) -> {
            IntTag idx = (IntTag)tag; // TODO: check type before casting
            Biome<?> biome;
            try {
                biome = Biome.of(biomeId);
            } catch (BiomeNotFoundException ex) {
                // TODO: for invalid biomes, either throw, ignore (no biome), or set to fallback
                biome = null; // nulls will be ignored
            }
            biomePalette.add(idx.getAsByte(), biome); // should check int to byte cast doesn't truncate the index
        });

        // Prepare to read data
        final byte[] biomesRaw = ctx.root.getByteArray(NBT_BIOME_DATA,
                (e) -> new SchematicReadException("Failed to read biome data", e));

        System.out.println(
                "biomesRaw size: " + biomesRaw.length + ", total blocks: " + (width * height * length)); // debug

        biomeVolume.setData(biomesRaw);
        biomeVolume.setPalette(biomePalette);

        ctx.builder.setBiomes(biomeVolume);
    }

}
