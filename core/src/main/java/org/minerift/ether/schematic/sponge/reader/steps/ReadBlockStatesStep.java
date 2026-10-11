package org.minerift.ether.schematic.sponge.reader.steps;

import org.minerift.ether.Ether;
import org.minerift.ether.math.Vec3i;
import org.minerift.ether.nms.world.block.BlockState;
import org.minerift.ether.schematic.SchematicReadException;
import org.minerift.ether.schematic.data.Array3DOrder;
import org.minerift.ether.schematic.data.BlockVolume;
import org.minerift.ether.schematic.data.BytePalette;
import org.minerift.ether.schematic.sponge.SpongeSchematic;
import org.minerift.ether.schematic.sponge.reader.SchematicReaderContext;
import org.minerift.ether.util.nbt.tags.IntTag;
import org.minerift.ether.util.nbt.tags.StringTag;
import org.minerift.ether.util.nbt.tags.container.CompoundTag;

import static java.lang.String.format;
import static org.minerift.ether.schematic.sponge.reader.SchematicNBTFields.*;
import static org.minerift.ether.util.nbt.tags.TagTypes.INT;

public class ReadBlockStatesStep implements IReaderStep {

    @Override
    public void read(SchematicReaderContext ctx) throws SchematicReadException {

        final SpongeSchematic.Builder builder = ctx.builder;
        final CompoundTag root = ctx.root;

        CompoundTag paletteRaw = root.getCompound(NBT_PALETTE,
                (e) -> new SchematicReadException("Failed to read block state palette", e) );

        BytePalette<BlockState<?>> palette;

        // Get/check palette size
        IntTag paletteMax = root.tryGetTag(NBT_PALETTE_MAX, INT);
        if(paletteMax != null) {
            if(paletteRaw.size() != paletteMax.getAsInt()) {
                // TODO: proper logger
                System.out.println(format("Expected a palette size of %d, but actually got %d", paletteMax.getAsInt(), paletteRaw.size()));
            }
            palette = BytePalette.of(paletteRaw.size());
        } else {
            palette = BytePalette.of();
        }

        // Map raw palette to actual palette
        paletteRaw.forEach((e) -> {
            // TODO: review this item/block name upgrader
            // TODO: create fixer-upper ops class for nunbt/other needs
            String data = e.getKey();
            data = Ether.inst().getNms().fixUpItemName(StringTag.valueOf(data), -1).getStrVal();

            // should probably check before casting; everything would fail regardless
            IntTag idx = (IntTag) e.getValue();

            BlockState<?> state = BlockState.of(data, null);
            if(state == null) {
                // TODO: proper logger
                System.out.println("Block state " + data + " failed to create, defaulting to air");
                state = BlockState.of("minecraft:air", null);
            }
            // should check if int has larger value than byte to handle wrapping; we'll worry about that later;
            // even if value is larger, the palette may already have an entry for that id; unreplaceable here

            //debug: System.out.println(idx.getAsByte() + " " + state);
            palette.add(idx.getAsByte(), state);
        });

        // Read block data
        byte[] blockDataRaw = root.getByteArray(NBT_BLOCK_DATA,
                (e) -> new SchematicReadException("Failed to read block data", e));
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
