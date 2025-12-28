package org.minerift.ether.schematic.sponge;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.minerift.ether.nms.world.Biome;
import org.minerift.ether.nms.world.block.BlockState;
import org.minerift.ether.schematic.SchematicReadException;
import org.minerift.ether.schematic.SchematicCodec;
import org.minerift.ether.schematic.data.BytePalette;
import org.minerift.ether.schematic.sponge.reader.SchematicReaderContext;
import org.minerift.ether.schematic.sponge.reader.steps.*;
import org.minerift.ether.util.nbt.Compression;
import org.minerift.ether.util.nbt.NbtReadException;
import org.minerift.ether.util.nbt.NbtReader;
import org.minerift.ether.util.nbt.NbtWriter;
import org.minerift.ether.util.nbt.tags.DoubleTag;
import org.minerift.ether.util.nbt.tags.IntTag;
import org.minerift.ether.util.nbt.tags.ShortTag;
import org.minerift.ether.util.nbt.tags.StringTag;
import org.minerift.ether.util.nbt.tags.array.ByteArrayTag;
import org.minerift.ether.util.nbt.tags.array.IntArrayTag;
import org.minerift.ether.util.nbt.tags.container.CompoundTag;
import org.minerift.ether.util.nbt.tags.container.ListTag;
import org.minerift.ether.util.nbt.tags.container.NoTagTypeFoundException;
import org.minerift.ether.world.BlockEntityArchetype;
import org.minerift.ether.world.EntityArchetype;

import java.io.File;
import java.io.IOException;

import static org.minerift.ether.schematic.sponge.reader.SchematicNBTFields.*;
import static org.minerift.ether.schematic.sponge.reader.SchematicNBTFields.NBT_BIOME_DATA;
import static org.minerift.ether.schematic.sponge.reader.SchematicNBTFields.NBT_BIOME_PALETTE;
import static org.minerift.ether.schematic.sponge.reader.SchematicNBTFields.NBT_BLOCK_DATA;
import static org.minerift.ether.schematic.sponge.reader.SchematicNBTFields.NBT_PALETTE;
import static org.minerift.ether.schematic.sponge.reader.SchematicNBTFields.NBT_VERSION;
import static org.minerift.ether.util.nbt.tags.TagTypes.COMPOUND;
import static org.minerift.ether.util.nbt.tags.TagTypes.DOUBLE;

public class SpongeSchematicCodec implements SchematicCodec<SpongeSchematic> {

    public static final SpongeSchematicCodec INST = new SpongeSchematicCodec();

    public static class ReadStages {
        public static final IReaderStep INIT             = new ReadInitStep();
        public static final IReaderStep METADATA         = new ReadMetadataStep();
        public static final IReaderStep BLOCK_STATES     = new ReadBlockStatesStep();
        public static final IReaderStep BLOCK_ENTITIES   = new ReadBlockEntitiesStep();
        public static final IReaderStep ENTITIES         = new ReadEntitiesStep();
        public static final IReaderStep BIOMES           = new ReadBiomesStep();

        private ReadStages() {}
    }

    @Override
    public SpongeSchematic read(File file) throws SchematicReadException {
        CompoundTag tag;
        try {
            NbtReader reader = NbtReader.from(file);
            tag = reader.readNextTag(COMPOUND);
        } catch (NoTagTypeFoundException | NbtReadException | IOException ex) {
            throw new SchematicReadException(ex);
        }
        return read(tag);
    }

    @Override
    public SpongeSchematic read(ByteBuf buf, Compression compress) throws SchematicReadException {
        CompoundTag tag;
        try {
            NbtReader reader = NbtReader.from(buf, compress);
            tag = reader.readNextTag(COMPOUND);
        } catch (IOException | NbtReadException | NoTagTypeFoundException ex) {
            throw new SchematicReadException(ex);
        }
        return read(tag);
    }

    @Override
    public SpongeSchematic read(CompoundTag tag) throws SchematicReadException {
        return readBuilder(tag).build();
    }

    public SpongeSchematic.Builder readBuilder(CompoundTag tag) throws SchematicReadException {
        final SchematicReaderContext ctx = new SchematicReaderContext(tag, SpongeSchematic.builder());

        ReadStages.INIT.read(ctx);
        ReadStages.METADATA.read(ctx);
        ReadStages.BLOCK_STATES.read(ctx);
        ReadStages.BLOCK_ENTITIES.read(ctx);
        ReadStages.BIOMES.read(ctx);
        ReadStages.ENTITIES.read(ctx);

        return ctx.builder;
    }

    @Override
    public int write(SpongeSchematic schem, ByteBuf buf, int flags) {
        NbtWriter writer = NbtWriter.from(buf);
        CompoundTag tag = writeAsNbt(schem, flags);
        return writer.writeTag(tag);
    }

    @Override
    public int write(SpongeSchematic schem, File file, int flags) throws IOException {
        NbtWriter writer = NbtWriter.from();
        CompoundTag schemTag = schem.writeAsNbt(flags);
        writer.writeTag(schemTag);
        return writer.dump(file);
    }

    @Override
    public CompoundTag writeAsNbt(SpongeSchematic schem, int flags) {
        CompoundTag root = new CompoundTag();

        root.addTag(new ShortTag(NBT_WIDTH, (short) schem.getWidth()));
        root.addTag(new ShortTag(NBT_HEIGHT, (short) schem.getHeight()));
        root.addTag(new ShortTag(NBT_LENGTH, (short) schem.getLength()));

        root.addTag(new IntTag(NBT_VERSION, 3)); // SpongeVersion.V3

        {
            CompoundTag palette = new CompoundTag(NBT_PALETTE);
            for (BytePalette.Entry<BlockState<?>> entry : schem.getBlocks().getPalette()) {
                palette.addTag(
                        new IntTag(entry.getValue().getAsString(), entry.getKey())
                );
            }
            root.addTag(palette);
            root.addTag(new ByteArrayTag(NBT_BLOCK_DATA, schem.getBlocks().getData()));
        }


        if(!schem.getBlocks().blockEntities.isEmpty()) {
            ListTag<CompoundTag> bEntities = new ListTag<>(NBT_BLOCK_ENTITIES, COMPOUND);

            for(Int2ObjectMap.Entry<BlockEntityArchetype> entry : schem.getBlocks().blockEntities.int2ObjectEntrySet()) {
                CompoundTag bEntity = new CompoundTag();
                bEntity.addTag( new StringTag(NBT_BLOCK_ENTITIES_ID, entry.getValue().getState().getAsString()) ); // TODO: test getAsString()
                bEntity.addTag( new IntArrayTag(NBT_BLOCK_ENTITIES_POS, entry.getValue().getPos().getXYZ()) );
            }
            root.addTag(bEntities);
        }

        if((flags & WRITE_BIOMES_ALWAYS) != 0 || ((flags & EXCLUDE_BIOMES) == 0 && !schem.getBiomes().isEmpty())) {
            CompoundTag palette = new CompoundTag(NBT_BIOME_PALETTE);
            for(BytePalette.Entry<Biome<?>> entry : schem.getBiomes().getPalette()) {
                palette.addTag(
                        new IntTag(entry.getValue().getResourceKey(), entry.getKey()) // TODO: test getResourceKey()
                );
            }

            root.addTag(palette);
            root.addTag(new ByteArrayTag(NBT_BIOME_DATA, schem.getBiomes().getData()));
        }

        if((flags & WRITE_ENTITIES_ALWAYS) != 0 || ((flags & EXCLUDE_ENTITIES) == 0 && !schem.getEntities().isEmpty())) {
            ListTag<CompoundTag> entities = new ListTag<>(NBT_ENTITIES, COMPOUND);

            for(EntityArchetype entry : schem.getEntities()) {
                CompoundTag entity = entry.getNbtData().copy();
                entity.addTag(new StringTag(NBT_ENTITIES_ID, entry.getId()));

                ListTag<DoubleTag> pos = new ListTag<>(NBT_ENTITIES_POS, DOUBLE);
                pos.addTag(DoubleTag.valueOf(entry.getPos().getXd()));
                pos.addTag(DoubleTag.valueOf(entry.getPos().getYd()));
                pos.addTag(DoubleTag.valueOf(entry.getPos().getZd()));
                entity.addTag(pos);

                entities.addTag(entity);
            }

            root.addTag(entities);
        }

        return root;
    }

    /*
    public void write(SpongeSchematic schem, File file) throws IOException {
        try(RandomAccessFile f = new RandomAccessFile(file, "rw")) {
            ByteBuf buf = Unpooled.buffer(1024);
            write(schem, buf, 0);
            buf.resetReaderIndex(); // review this; if marked, marker should be appropriate
            buf.readBytes(f.getChannel(), 0, buf.readableBytes());
        }
    }
    */
}
