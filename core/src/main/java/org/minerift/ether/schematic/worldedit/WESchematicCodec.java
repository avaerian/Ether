package org.minerift.ether.schematic.worldedit;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.DataFixer;
import com.sk89q.worldedit.world.block.BlockTypes;
import io.netty.buffer.ByteBuf;
import org.minerift.ether.nms.world.block.BlockState;
import org.minerift.ether.schematic.SchematicReadException;
import org.minerift.ether.schematic.SchematicCodec;
import org.minerift.ether.schematic.data.BytePalette;
import org.minerift.ether.schematic.sponge.SpongeSchematic;
import org.minerift.ether.schematic.sponge.SpongeSchematicCodec;
import org.minerift.ether.util.UnreachableException;
import org.minerift.ether.util.nbt.tags.container.CompoundTag;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static org.minerift.ether.schematic.data.Array3DOrder.YZX;

public class WESchematicCodec implements SchematicCodec<WorldEditSchematic> {

    @Override
    public WorldEditSchematic read(File file) throws SchematicReadException {
        try {
            Clipboard clipboard = ClipboardFormats.findByFile(file).getReader(new FileInputStream(file)).read();
            return new WorldEditSchematic(clipboard);
        } catch (IOException ex) {
            throw new SchematicReadException("Failed to read schematic via WorldEdit", ex);
        }
    }

    // hacky idea: create temp file to wrap buffer; thanks WorldEdit for the shitty API :)
    // originally planned to have IO from WorldEdit, however the API looks abysmal, so let's do our own
    @Override
    public WorldEditSchematic read(ByteBuf buf) throws SchematicReadException {
        throw new UnreachableException("unimplemented");
    }

    @Override
    public WorldEditSchematic read(CompoundTag tag) throws SchematicReadException {
        SpongeSchematic.Builder data = SpongeSchematicCodec.INST.readBuilder(tag);

        // FIXME
        Region region = new CuboidRegion(BlockVector3.ZERO,
                BlockVector3.at(data.getWidth(), data.getHeight(), data.getLength()));

        Clipboard clipboard = new BlockArrayClipboard(region);

        // Translate blk states to WorldEdit blk states
        BytePalette<BlockState<?>> blkPlt = data.getBlocks().getPalette();
        BytePalette<com.sk89q.worldedit.world.block.BlockState> blkPltWE = BytePalette.of(blkPlt.size());

        byte[] blkIds = data.getBlocks().getData();

        ParserContext parserContext = new ParserContext();
        parserContext.setRestricted(false);
        parserContext.setTryLegacy(false);
        parserContext.setPreferringWildcard(false);

        for(BytePalette.Entry<BlockState<?>> entry : blkPlt) {
            com.sk89q.worldedit.world.block.BlockState state;

            // Upgrading should already be done in SpongeSchematicCodec
            try {
                state = WorldEdit.getInstance().getBlockFactory()
                        .parseFromInput(entry.getValue().getAsString(), parserContext)
                        .toImmutableState();
            } catch (InputParseException e) {
                //LOGGER.warn("Invalid BlockState in palette: " + palettePart + ". Block will be replaced with air.");
                state = BlockTypes.AIR.getDefaultState();
            }
            blkPltWE.add(entry.getKey(), state);
        }

        // Set blocks
        for(int y = 0; y < data.getHeight(); y++) {
            for(int z = 0; z < data.getLength(); z++) {
                for(int x = 0; x < data.getWidth(); x++) {
                    BlockVector3 pos = BlockVector3.at(x, y, z);
                    byte id = blkIds[YZX.flatten(data.getWidth(), data.getLength(), x, y, z)];
                    try {
                        clipboard.setBlock(pos, blkPltWE.get(id));
                    } catch (WorldEditException e) {
                        throw new SchematicReadException("Failed to set block in WorldEdit clipboard", e);
                    }
                }
            }
        }

        return null;
    }

    @Override
    public int write(WorldEditSchematic schem, ByteBuf buf, int flags) {
        return 0;
    }

    @Override
    public int write(WorldEditSchematic schem, File file, int flags) {
        return 0;
    }

    @Override
    public CompoundTag writeAsNbt(WorldEditSchematic schem, int flags) {
        return null;
    }
}