package org.minerift.ether.schematic.sponge.reader.steps;

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

import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;
import static org.minerift.ether.schematic.sponge.reader.SchematicNBTFields.*;

public class ReadBiomesStep implements IReaderStep {

    @Override
    public void read(SchematicReaderContext ctx) throws SchematicFileReadException {

        final int width = ctx.builder.getWidth();
        final int height = ctx.builder.getHeight();
        final int length = ctx.builder.getLength();

        final Optional<CompoundTag> tagList = ctx.root.getCompound(NBT_BIOME_PALETTE);

        BiomeVolume.Builder biomeVolume = BiomeVolume.builder()
                .setOrder(Array3DOrder.YZX)
                .setDimensions(width, height, length);
        if(tagList.isPresent()) {

            // Read biome palette
            Map<String, Tag> biomePaletteRaw = tagList.get().getValue();
            //Int2ObjectMap<String> biomePalette = new Int2ObjectOpenHashMap<>(biomePaletteRaw.size());
            BytePalette<Biome<?>> biomePalette = new BytePalette<>(biomePaletteRaw.size());

            // Verify size
            ctx.root.getInt(NBT_BIOME_PALETTE_MAX).ifPresent((expectedSize) -> {
                if(biomePaletteRaw.size() != expectedSize) {
                    // TODO: proper logger
                    System.out.println(format("Expected a palette size of %d, but actually got %d", expectedSize, biomePaletteRaw.size()));
                }
            });

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
            final byte[] biomesRaw = ctx.root.getByteArray(NBT_BIOME_DATA).orElseThrow(() -> new SchematicFileReadException("Failed to read biome data!"));

            // TODO: for debug
            System.out.println("biomesRaw size: " + biomesRaw.length + ", total blocks: " + (width * height * length));

            biomeVolume.setData(biomesRaw);
            biomeVolume.setPalette(biomePalette);
        } else {
            BytePalette<String> biomePalette = new BytePalette<>(1);
            biomePalette.add((byte) 0, "minecraft:air");
            biomeVolume.setData(new byte[width * height * length]); // TODO: fix this with EmptyBiomeVolume
        }
        ctx.builder.setBiomes(biomeVolume);
    }

}
