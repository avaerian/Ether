package org.minerift.ether.schematic.readers.sponge.steps;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.bukkit.block.Biome;
import org.minerift.ether.schematic.SchematicFileReadException;
import org.minerift.ether.schematic.readers.sponge.SchematicReaderContext;
import org.minerift.ether.math.Vec3i;
import org.minerift.ether.util.nbt.NBTSectionView;
import org.minerift.ether.util.nbt.tags.IntTag;
import org.minerift.ether.util.nbt.tags.Tag;
import org.minerift.ether.world.BiomeArchetype;
import org.minerift.ether.world.BiomesList;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

import static org.minerift.ether.schematic.readers.sponge.SchematicNBTFields.*;

public class ReadBiomesStep implements IReaderStep {

    @Override
    public void read(SchematicReaderContext ctx) throws SchematicFileReadException {

        final short width = ctx.builder.getWidth();
        final short height = ctx.builder.getHeight();
        final short length = ctx.builder.getLength();

        final Optional<NBTSectionView> tagList = ctx.rootView.getSectionView(NBT_BIOME_PALETTE);
        if(tagList.isPresent()) {

            // Read biome palette
            Map<String, Tag> biomePaletteRaw = tagList.get().getSectionTags();
            Int2ObjectMap<Biome> biomePalette = new Int2ObjectOpenHashMap<>(biomePaletteRaw.size());

            // Verify size
            ctx.rootView.getInt(NBT_BIOME_PALETTE_MAX).ifPresent((expectedSize) -> {
                if(biomePaletteRaw.size() != expectedSize) {
                    // TODO: proper logger
                    System.out.println(String.format("Expected a palette size of %d, but actually got %d", expectedSize, biomePaletteRaw.size()));
                }
            });

            // Map from raw palette to actual palette
            biomePaletteRaw.forEach((rawData, idx) -> biomePalette.put(((IntTag)idx).getValue().intValue(), BiomesList.getBiome(rawData)));

            // Prepare to read data
            final byte[] biomesRaw = ctx.rootView.getByteArray(NBT_BIOME_DATA).orElseThrow(() -> new SchematicFileReadException("Failed to read biome data!"));
            ctx.builder.setBiomes(new ArrayList<>(width * height * length));

            // Read data
            for(int x = 0; x < width; x++) {
                for(int z = 0; z < length; z++) {

                    final int index = x + (z * width);
                    final int biomeId = biomesRaw[index] & 0xFF;
                    final Biome biome = biomePalette.get(biomeId);

                    for(int y = 0; y < height; y++) {
                        ctx.builder.getBiomes().add(new BiomeArchetype(biome, new Vec3i.Mutable(x, y, z)));
                    }
                }
            }
        }
    }

}
