package org.minerift.ether.schematic.readers;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.bukkit.Bukkit;
import org.bukkit.block.data.BlockData;
import org.jnbt.*;
import org.minerift.ether.schematic.objects.SchematicBlockEntity;
import org.minerift.ether.schematic.NBTTagReader;
import org.minerift.ether.schematic.objects.SchematicEntity;
import org.minerift.ether.schematic.types.SpongeSchematic;
import org.minerift.ether.util.math.Vec3d;
import org.minerift.ether.util.math.Vec3i;
import org.minerift.ether.world.QueuedBlock;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Deprecated
public class DeprecatedSpongeSchematicReader {

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

    private DeprecatedSpongeSchematicReader() {}

    // TODO: temporarily test stuffs
    public static void main(String[] args) throws IOException {
        File schemFile = new File("C:\\Users\\avaer\\Downloads\\testisland3.schem");
        SpongeSchematic.Builder builder = read(schemFile);

        System.out.println(String.format("%d, %d, %d", builder.getWidth(), builder.getLength(), builder.getHeight()));
        System.out.println(String.format("Schematic Version: %d", builder.getVersion()));
        System.out.println(Arrays.deepToString(builder.getBlocksNew().toArray(QueuedBlock[]::new)));
        System.out.println(Arrays.deepToString(builder.getBlockEntities().toArray(SchematicBlockEntity[]::new)));
        System.out.println(Arrays.deepToString(builder.getEntities().toArray(SchematicEntity[]::new)));
        System.out.println(Arrays.deepToString(builder.getBiomes()));

    }

    public static SpongeSchematic.Builder read(File file) throws IOException {

        SpongeSchematic.Builder builder = null;

        try(FileInputStream fis = new FileInputStream(file); NBTInputStream nbt = new NBTInputStream(fis)) {

            builder = new SpongeSchematic.Builder();
            CompoundTag head = (CompoundTag) nbt.readTag();

            builder.setVersion(NBTTagReader.getIntOrThrow(head, NBT_VERSION));
            builder.setWidth(NBTTagReader.getShortOrThrow(head, NBT_WIDTH));
            builder.setHeight(NBTTagReader.getShortOrThrow(head, NBT_HEIGHT));
            builder.setLength(NBTTagReader.getShortOrThrow(head, NBT_LENGTH));

            readBlockStates(builder, head);
            readBlockEntities(builder, head);
            readEntities(builder, head);
            readBiomes(builder, head);

            System.out.println(String.format("%d blocks", builder.getWidth() * builder.getHeight() * builder.getLength()));

        }

        return builder;
    }

    // Referencing: https://github.com/SandroHc/schematic4j/blob/master/src/main/java/net/sandrohc/schematic4j/parser/SpongeSchematicParser.java#L152
    private static void readBlockStates(SpongeSchematic.Builder builder, CompoundTag head) {

        // Read block palette
        Int2ObjectMap<BlockData> palette = new Int2ObjectArrayMap<>();
        Map<String, Tag> paletteRaw = NBTTagReader.getCompoundMapOrThrow(head, NBT_PALETTE);
        paletteRaw.forEach((rawData, idx) -> palette.put(((IntTag)idx).getValue().intValue(), Bukkit.createBlockData(rawData)));

        // Verify palette size
        int expectedPaletteSize = NBTTagReader.getIntOrThrow(head, NBT_PALETTE_MAX);
        if(palette.size() != expectedPaletteSize) {
            // TODO: move from println to proper logger
            System.out.println(String.format("Expected a palette size of %d, but actually got %d", expectedPaletteSize, palette.size()));
        }

        // Prepare to read block data
        byte[] blockDataRaw = NBTTagReader.getByteArrayOrThrow(head, NBT_BLOCK_DATA);
        //builder.setBlocks(new BlockData[builder.getWidth()][builder.getHeight()][builder.getLength()]);
        builder.setBlocksNew(new HashSet<>(builder.getWidth() * builder.getHeight() * builder.getLength()));

        // Verify block count
        int expectedBlockCount = builder.getWidth() * builder.getHeight() * builder.getLength();
        if(blockDataRaw.length != expectedBlockCount) {
            // TODO: move from println to proper logger
            System.out.println(String.format("Expected a block count of %d, but actually got %d", expectedBlockCount, blockDataRaw.length));
        }

        // Read block data into 3d buffer
        for(int x = 0; x < builder.getWidth(); x++) {
            for(int y = 0; y < builder.getHeight(); y++) {
                for(int z = 0; z < builder.getLength(); z++) {
                    final int index = x + (z * builder.getWidth()) + (y * builder.getWidth() * builder.getLength());
                    final int blockId = blockDataRaw[index] & 0xFF;
                    final BlockData block = palette.get(blockId);

                    //builder.getBlocks()[x][y][z] = block;
                    builder.getBlocksNew().add(new QueuedBlock(new Vec3i.Mutable(x, y, z), block));
                }
            }
        }
    }

    // Referencing: https://github.com/SandroHc/schematic4j/blob/master/src/main/java/net/sandrohc/schematic4j/parser/SpongeSchematicParser.java#L256C24-L256C24
    private static void readBlockEntities(SpongeSchematic.Builder builder, CompoundTag head) {

        // TODO: note that this is keeping schematic specification 3 in-mind and need to have backwards support for 2 and 1
        Optional<List<CompoundTag>> possibleBlockEntitiesRaw = NBTTagReader.findCompoundListTag(head, NBT_BLOCK_ENTITIES);
        if(possibleBlockEntitiesRaw.isPresent()) {

            // Read NBT data into schematic
            List<CompoundTag> blockEntitiesRaw = possibleBlockEntitiesRaw.get();
            builder.setBlockEntities(new HashSet<>(blockEntitiesRaw.size()));
            for(CompoundTag blockEntity : blockEntitiesRaw) {

                String id = NBTTagReader.getStringOrThrow(blockEntity, NBT_BLOCK_ENTITIES_ID);
                int[] pos = NBTTagReader.getIntArrayOrThrow(blockEntity, NBT_BLOCK_ENTITIES_POS);

                // "extra" field appears to be a bunch of misc properties that im not entirely sure of
                // we'll implement it for now and test later
                Map<String, Object> extra = blockEntity.getValue().entrySet().stream()
                        .filter(entry -> !entry.getKey().equals(NBT_BLOCK_ENTITIES_ID) && !entry.getKey().equals(NBT_BLOCK_ENTITIES_POS))
                        .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getValue())); // TODO: no "value unwrapping" is done; check for bugs

                builder.getBlockEntities().add(new SchematicBlockEntity(id, new Vec3i(pos), extra));

            }
        } else {
            // TODO: move from println to proper logger
            System.out.println("No block entities found");
            builder.setBlockEntities(Collections.emptySet());
        }
    }

    private static void readEntities(SpongeSchematic.Builder builder, CompoundTag head) {

        Optional<List<CompoundTag>> possibleEntitiesRaw = NBTTagReader.findCompoundListTag(head, NBT_ENTITIES);
        if(possibleEntitiesRaw.isPresent()) {

            // Prepare NBT entity data
            List<CompoundTag> entitiesRaw = possibleEntitiesRaw.get();
            builder.setEntities(new HashSet<>(entitiesRaw.size()));

            for(CompoundTag entity : entitiesRaw) {

                // Read entity data
                final String id = NBTTagReader.getStringOrThrow(entity, NBT_ENTITIES_ID);

                final double[] posRaw = NBTTagReader.getListOrThrow(entity, NBT_ENTITIES_POS).stream().mapToDouble(tag -> ((DoubleTag) tag).getValue()).toArray();
                final Vec3d pos = new Vec3d(posRaw);

                final Map<String, Object> extra = entity.getValue().entrySet().stream()
                        .filter(entry -> !entry.getKey().equals(NBT_ENTITIES_ID) && !entry.getKey().equals(NBT_ENTITIES_POS))
                        .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getValue())); // TODO: no "value unwrapping" is done; check for bugs

                // Append schematic entity
                builder.getEntities().add(new SchematicEntity(id, pos, extra));
            }
        } else {
            // TODO: move from println to proper logger
            System.out.println("No entities found");
            builder.setEntities(Collections.emptySet());
        }
    }

    private static void readBiomes(SpongeSchematic.Builder builder, CompoundTag head) {

        Optional<CompoundTag> possiblePaletteRaw = NBTTagReader.findCompoundTag(head, NBT_BIOME_PALETTE);
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
        final int expectedPaletteSize = NBTTagReader.getIntOrThrow(head, NBT_BIOME_PALETTE_MAX);
        if(palette.size() != expectedPaletteSize) {
            // TODO: move from println to proper logger
            System.out.println(String.format("Expected a palette size of %d, but actually got %d", expectedPaletteSize, palette.size()));
        }

        // Prepare raw data
        byte[] biomesRaw = NBTTagReader.getByteArrayOrThrow(head, NBT_BIOME_DATA);
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

}
