package org.minerift.ether.schematic.readers;

import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.bukkit.block.Biome;
import org.minerift.ether.util.nbt.tags.CompoundTag;
import org.minerift.ether.util.nbt.tags.IntTag;
import org.minerift.ether.util.nbt.NBTInputStream;
import org.minerift.ether.util.nbt.tags.StringTag;
import org.minerift.ether.util.nbt.tags.Tag;
import org.minerift.ether.schematic.types.SpongeSchematic;
import org.minerift.ether.util.math.Vec3d;
import org.minerift.ether.util.math.Vec3i;
import org.minerift.ether.util.nbt.NBTSectionView;
import org.minerift.ether.world.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class SpongeSchematicReader {

    public static final String NBT_VERSION = "Version";
    public static final String NBT_DATA_VERSION = "DataVersion";
    public static final String NBT_METADATA = "Metadata";
    public static final String NBT_METADATA_NAME = "Name";
    public static final String NBT_METADATA_AUTHOR = "Author";
    public static final String NBT_METADATA_DATE = "Date";
    public static final String NBT_METADATA_REQUIRED_MODS = "RequiredMods";
    public static final String NBT_WIDTH = "Width";
    public static final String NBT_HEIGHT = "Height";
    public static final String NBT_LENGTH = "Length";
    public static final String NBT_OFFSET = "Offset";
    public static final String NBT_PALETTE = "Palette";
    public static final String NBT_PALETTE_MAX = "PaletteMax";
    public static final String NBT_BLOCK_DATA = "BlockData";
    public static final String NBT_BLOCK_ENTITIES = "BlockEntities";
    public static final String NBT_BLOCK_ENTITIES_ID = "Id";
    public static final String NBT_BLOCK_ENTITIES_POS = "Pos";
    public static final String NBT_TILE_ENTITIES = "TileEntities";
    public static final String NBT_BIOME_PALETTE = "BiomePalette";
    public static final String NBT_BIOME_PALETTE_MAX = "BiomePaletteMax";
    public static final String NBT_BIOME_DATA = "BiomeData";
    public static final String NBT_ENTITIES = "Entities";
    public static final String NBT_ENTITIES_ID = "Id";
    public static final String NBT_ENTITIES_POS = "Pos";

    private SpongeSchematicReader() {}

    // TODO: temporarily test stuffs
    public static void main(String[] args) throws IOException {
        File schemFile = new File("C:\\Users\\avaer\\Downloads\\testisland2.schem");
        SpongeSchematic.Builder builder = new SpongeSchematic.Builder();
        read(schemFile, builder);

        System.out.println(String.format("%d, %d, %d", builder.getWidth(), builder.getLength(), builder.getHeight()));
        System.out.println(String.format("Schematic Version: %d", builder.getVersion()));

        BlockArchetype[] blocks = new ArrayList<>(builder.getBlocks()).stream().filter(block -> !block.getId().contains("minecraft:air")).toArray(BlockArchetype[]::new);
        BlockEntityArchetype[] blockEntities = Arrays.stream(blocks).filter(block -> block instanceof BlockEntityArchetype).toArray(BlockEntityArchetype[]::new);

        System.out.println(Arrays.deepToString(blocks));
        System.out.println(Arrays.deepToString(blockEntities));
        System.out.println(Arrays.deepToString(builder.getEntities().toArray(EntityArchetype[]::new)));
        System.out.println(Arrays.deepToString(builder.getBiomes().toArray(BiomeArchetype[]::new)));
    }

    public static SpongeSchematic.Builder read(File file, SpongeSchematic.Builder builder) throws IOException {

        try(FileInputStream fis = new FileInputStream(file); NBTInputStream nbt = new NBTInputStream(fis)) {

            CompoundTag tag = (CompoundTag) nbt.readTag();
            NBTSectionView rootView = new NBTSectionView(tag);

            // Read initial data
            builder.setVersion(rootView.getInt(NBT_VERSION).orElseThrow());
            builder.setWidth(rootView.getShort(NBT_WIDTH).orElseThrow());
            builder.setHeight(rootView.getShort(NBT_HEIGHT).orElseThrow());
            builder.setLength(rootView.getShort(NBT_LENGTH).orElseThrow());

            readBlockEntities(builder, rootView);
            readBlockStates(builder, rootView);
            readEntities(builder, rootView);
            readBiomes(builder, rootView);

            System.out.println(String.format("%d blocks", builder.getWidth() * builder.getHeight() * builder.getLength()));

        } catch (NoSuchElementException ex) {
            throw new RuntimeException(ex);
        }

        return builder;
    }

    private static void readBlockStates(SpongeSchematic.Builder builder, NBTSectionView rootView) {

        final short width = builder.getWidth();
        final short height = builder.getHeight();
        final short length = builder.getLength();

        // Prepare to parse palette
        Int2ObjectMap<String> palette = new Int2ObjectOpenHashMap<>();
        Map<String, Tag> paletteRaw = rootView.getSectionView(NBT_PALETTE).orElseThrow().getSectionTags();

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
        byte[] blockDataRaw = rootView.getByteArray(NBT_BLOCK_DATA).orElseThrow();
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

    private static void readBlockEntities(SpongeSchematic.Builder builder, NBTSectionView rootView) {

        final String blockEntitiesKey = builder.getVersion() == 1 ? NBT_TILE_ENTITIES : NBT_BLOCK_ENTITIES;

        rootView.getList(blockEntitiesKey).ifPresentOrElse((tagList) -> {

            List<NBTSectionView> blockEntitiesRaw = tagList.stream().map(tag -> new NBTSectionView((CompoundTag)tag)).toList();
            builder.setBlockEntities(new HashMap<>(blockEntitiesRaw.size()));
            for(NBTSectionView blockEntity : blockEntitiesRaw) {

                final String id = blockEntity.getString(NBT_BLOCK_ENTITIES_ID).orElseThrow();
                final int[] rawPos = blockEntity.getIntArray(NBT_BLOCK_ENTITIES_POS).orElseThrow();
                final Vec3i pos = new Vec3i.Mutable(rawPos);

                // Fix up NBT data
                Map<String, Tag> rawNbt = Maps.newHashMap(blockEntity.getSectionTags());

                rawNbt.put("x", new IntTag("x", pos.getX()));
                rawNbt.put("y", new IntTag("y", pos.getY()));
                rawNbt.put("z", new IntTag("z", pos.getZ()));
                rawNbt.put("id", new StringTag("id", id));

                rawNbt.remove(NBT_ENTITIES_POS);
                rawNbt.remove(NBT_ENTITIES_ID);

                CompoundTag fixedNbt = new CompoundTag(blockEntity.getName(), rawNbt);

                builder.getBlockEntities().put(pos, new BlockEntityArchetype(id, pos, fixedNbt));
            }
        },
        // Else
        () -> builder.setBlockEntities(Collections.emptyMap()));
    }

    private static void readEntities(SpongeSchematic.Builder builder, NBTSectionView rootView) {

        rootView.getList(NBT_ENTITIES).ifPresentOrElse((tagList) -> {

            List<NBTSectionView> entitiesRaw = tagList.stream().map(tag -> new NBTSectionView((CompoundTag)tag)).toList();
            builder.setEntities(new HashSet<>(entitiesRaw.size()));

            for(NBTSectionView entity : entitiesRaw) {

                final String id = entity.getString(NBT_ENTITIES_ID).orElseThrow();
                final Double[] posRaw = entity.getDoubleArray(NBT_ENTITIES_POS).orElseThrow();
                final Vec3d.Mutable pos = new Vec3d.Mutable(posRaw[0], posRaw[1], posRaw[2]);

                builder.getEntities().add(new EntityArchetype(id, pos));
            }
        },
        // Else
        () -> builder.setEntities(Collections.emptySet()));
    }

    private static void readMetadata(SpongeSchematic.Builder builder, NBTSectionView rootView) {

        // TODO

    }

    private static void readBiomes(SpongeSchematic.Builder builder, NBTSectionView rootView) {

        final short width = builder.getWidth();
        final short height = builder.getHeight();
        final short length = builder.getLength();

        rootView.getSectionView(NBT_BIOME_PALETTE).ifPresentOrElse((tagList) -> {

            // Read biome palette
            Map<String, Tag> biomePaletteRaw = tagList.getSectionTags();
            Int2ObjectMap<Biome> biomePalette = new Int2ObjectOpenHashMap<>();

            // Verify size
            rootView.getInt(NBT_BIOME_PALETTE_MAX).ifPresent((expectedSize) -> {
                if(biomePaletteRaw.size() != expectedSize) {
                    // TODO: proper logger
                    System.out.println(String.format("Expected a palette size of %d, but actually got %d", expectedSize, biomePaletteRaw.size()));
                }
            });

            // Map from raw palette to actual palette
            biomePaletteRaw.forEach((rawData, idx) -> biomePalette.put(((IntTag)idx).getValue().intValue(), BiomesList.getBiome(rawData)));

            // Prepare to read data
            final byte[] biomesRaw = rootView.getByteArray(NBT_BIOME_DATA).orElseThrow();
            builder.setBiomes(new ArrayList<>(width * height * length));

            // Read data
            for(int x = 0; x < width; x++) {
                for(int z = 0; z < length; z++) {

                    final int index = x + (z * width);
                    final int biomeId = biomesRaw[index] & 0xFF;
                    final Biome biome = biomePalette.get(biomeId);

                    for(int y = 0; y < height; y++) {
                        builder.getBiomes().add(new BiomeArchetype(biome, new Vec3i.Mutable(x, y, z)));
                    }
                }
            }

        },
        // Else
        () -> builder.setBiomes(Collections.emptyList()));
    }

/*
    @Deprecated
    private static void readBiomes(SpongeSchematic.Builder builder, CompoundTag head) {

        Optional<CompoundTag> possiblePaletteRaw = NBTUtils.findCompoundTag(head, NBT_BIOME_PALETTE);
        if(possiblePaletteRaw.isEmpty()) {
            System.out.println("No biome data found");
            builder.setBiomes(new String[0][0]);
            return;
        }

        // Read biome palette from NBT
        Int2ObjectMap<String> palette = new Int2ObjectArrayMap<>();
        CompoundTag paletteRaw = possiblePaletteRaw.get();
        paletteRaw.getValue().forEach((biome, idx) -> palette.put(((IntTag)idx).getValue().intValue(), biome));

        // Verify palette size
        final int expectedPaletteSize = NBTUtils.getIntOrThrow(head, NBT_BIOME_PALETTE_MAX);
        if(palette.size() != expectedPaletteSize) {
            // TODO: move from println to proper logger
            System.out.println(String.format("Expected a palette size of %d, but actually got %d", expectedPaletteSize, palette.size()));
        }

        // Prepare raw data
        byte[] biomesRaw = NBTUtils.getByteArrayOrThrow(head, NBT_BIOME_DATA);
        builder.setBiomes(new String[builder.getWidth()][builder.getLength()]);

        // Load biome data into 2d buffer
        for(int x = 0; x < builder.getWidth(); x++) {
            for(int z = 0; z < builder.getLength(); z++) {
                final int index = x + (z * builder.getWidth());
                final int biomeId = biomesRaw[index] & 0xFF;
                final String biome = palette.get(biomeId);
                builder.getBiomes()[x][z] = biome;
            }
        }
    }
 */

}
