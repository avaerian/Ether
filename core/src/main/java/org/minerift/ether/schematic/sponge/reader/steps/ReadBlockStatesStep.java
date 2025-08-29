package org.minerift.ether.schematic.sponge.reader.steps;

import org.minerift.ether.Ether;
import org.minerift.ether.math.Vec3i;
import org.minerift.ether.nms.world.block.BlockState;
import org.minerift.ether.schematic.SchematicFileReadException;
import org.minerift.ether.schematic.data.Array3DOrder;
import org.minerift.ether.schematic.data.BlockVolume;
import org.minerift.ether.schematic.data.BytePalette;
import org.minerift.ether.schematic.sponge.SpongeSchematic;
import org.minerift.ether.schematic.sponge.reader.SchematicReaderContext;
import org.minerift.ether.util.nbt.tags.IntTag;
import org.minerift.ether.util.nbt.tags.StringTag;
import org.minerift.ether.util.nbt.tags.Tag;
import org.minerift.ether.util.nbt.tags.container.CompoundTag;

import java.util.Map;
import java.util.OptionalInt;

import static java.lang.String.format;
import static org.minerift.ether.schematic.sponge.reader.SchematicNBTFields.*;

public class ReadBlockStatesStep implements IReaderStep {

    @Override
    public void read(SchematicReaderContext ctx) throws SchematicFileReadException {

        final SpongeSchematic.Builder builder = ctx.builder;
        final CompoundTag root = ctx.root;

        /*final int width = builder.getWidth();
        final int height = builder.getHeight();
        final int length = builder.getLength();*/

        Map<String, Tag> paletteRaw = root.getCompound(NBT_PALETTE)
                .orElseThrow(() -> new SchematicFileReadException("Failed to read block state palette!"))
                .getValue();

        BytePalette<BlockState<?>> palette;

        // Get palette size
        OptionalInt paletteMax = root.getInt(NBT_PALETTE_MAX);
        if(paletteMax.isPresent()) {
            if(paletteRaw.size() != paletteMax.getAsInt()) {
                // TODO: proper logger
                System.out.println(format("Expected a palette size of %d, but actually got %d", paletteMax.getAsInt(), paletteRaw.size()));
            }
            palette = new BytePalette<>(paletteRaw.size());
        } else {
            palette = new BytePalette<>();
        }

        // Map raw palette to actual palette
        paletteRaw.forEach((data, idx) -> {

            // FIXME: review this item/block name upgrader
            // TODO: create fixer-upper ops class for nunbt/other needs
            data = Ether.getNms().fixUpItemName(StringTag.valueOf(data), -1).getValue();

            BlockState<?> state = BlockState.of(data, null);
            if(state == null) {
                // TODO: proper logger
                System.out.println("Block state " + data + " failed to create, defaulting to air");
                state = BlockState.of("minecraft:air", null);
            }
            System.out.println((byte)((IntTag)idx).getAsInt() + " " + state);
            palette.add((byte)((IntTag)idx).getAsInt(), state);
        });

        // Read block data
        byte[] blockDataRaw = root.getByteArray(NBT_BLOCK_DATA).orElseThrow(() -> new SchematicFileReadException("Failed to read block data!"));
        Vec3i dim = builder.getDimensions();
        System.out.println("blockDataRaw: " + blockDataRaw.length + ", dim: " + dim.getX() * dim.getY() * dim.getZ());

        BlockVolume.Builder blocks = BlockVolume.builder()
                .setOrder(Array3DOrder.YZX)
                .setDimensions(dim)
                .setData(blockDataRaw)
                .setPalette(palette);

        ctx.builder.setBlocks(blocks);
    }
}
