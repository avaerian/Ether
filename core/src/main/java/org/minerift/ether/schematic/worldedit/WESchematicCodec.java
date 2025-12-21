package org.minerift.ether.schematic.worldedit;

import com.sk89q.jnbt.NBTOutputStream;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.extent.clipboard.io.SpongeSchematicWriter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.block.BlockTypes;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import org.minerift.ether.nms.world.block.BlockState;
import org.minerift.ether.schematic.SchematicReadException;
import org.minerift.ether.schematic.SchematicCodec;
import org.minerift.ether.schematic.data.BytePalette;
import org.minerift.ether.schematic.sponge.SpongeSchematic;
import org.minerift.ether.schematic.sponge.SpongeSchematicCodec;
import org.minerift.ether.util.UnreachableException;
import org.minerift.ether.util.nbt.Compression;
import org.minerift.ether.util.nbt.NbtReadException;
import org.minerift.ether.util.nbt.NbtReader;
import org.minerift.ether.util.nbt.tags.container.CompoundTag;
import org.minerift.ether.util.nbt.tags.container.NoTagTypeFoundException;

import java.io.*;
import java.util.zip.GZIPOutputStream;

import static org.minerift.ether.schematic.data.Array3DOrder.YZX;
import static org.minerift.ether.util.nbt.tags.TagTypes.COMPOUND;

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

    @Override
    public WorldEditSchematic read(ByteBuf buf, Compression compress) throws SchematicReadException {
        try {
            NbtReader reader = NbtReader.from(buf, compress);
            CompoundTag root = reader.readNextTag(COMPOUND);
            return read(root);
        } catch (IOException | NbtReadException | NoTagTypeFoundException e) {
            throw new SchematicReadException(e);
        }
    }

    @Override
    public WorldEditSchematic read(CompoundTag tag) throws SchematicReadException {
        SpongeSchematic.Builder data = SpongeSchematicCodec.INST.readBuilder(tag);

        // FIXME
        Region region = new CuboidRegion(BlockVector3.ZERO,
                BlockVector3.at(data.getWidth(), data.getHeight(), data.getLength()));

        Clipboard clipboard = new BlockArrayClipboard(region);

        // Translate blk states to WorldEdit blk states
        BytePalette<BlockState<?>> blkPlt = data.getBlocks().getRight().getPalette();
        BytePalette<com.sk89q.worldedit.world.block.BlockState> blkPltWE = BytePalette.of(blkPlt.size());

        byte[] blkIds = data.getBlocks().getRight().getData();

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
        try { // block states should all be valid; if fail, something is very wrong
            for(int y = 0; y < data.getHeight(); y++) {
                for(int z = 0; z < data.getLength(); z++) {
                    for(int x = 0; x < data.getWidth(); x++) {
                        BlockVector3 pos = BlockVector3.at(x, y, z);
                        byte id = blkIds[YZX.flatten(data.getWidth(), data.getLength(), x, y, z)];
                        clipboard.setBlock(pos, blkPltWE.get(id));
                    }
                }
            }
        } catch (WorldEditException e) {
            throw new SchematicReadException("Failed to set block in WorldEdit clipboard", e);
        }
        return null;
    }

    @Override
    public int write(WorldEditSchematic schem, ByteBuf buf, int flags) {
        try (ByteBufOutputStream _out = new ByteBufOutputStream(buf);
            NBTOutputStream out = new NBTOutputStream(_out);
            SpongeSchematicWriter writer = new SpongeSchematicWriter(out)) {
            writer.write(schem.getClipboard());
            return buf.readableBytes();
        } catch (IOException e) {
            throw new UnreachableException("unexpected", e);
        }
    }

    @Override
    public int write(WorldEditSchematic schem, File file, int flags) throws IOException {
        ByteBuf buf = Unpooled.buffer(1024);
        try (GZIPOutputStream _out = new GZIPOutputStream(new ByteBufOutputStream(buf));
            NBTOutputStream out = new NBTOutputStream(_out);
            SpongeSchematicWriter writer = new SpongeSchematicWriter(out);
            RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            writer.write(schem.getClipboard());
            int bytes = buf.readableBytes();
            return buf.readBytes(raf.getChannel(), raf.getFilePointer(), bytes);
        }
    }

    @Override
    public CompoundTag writeAsNbt(WorldEditSchematic schem, int flags) {
        return null;
    }
}