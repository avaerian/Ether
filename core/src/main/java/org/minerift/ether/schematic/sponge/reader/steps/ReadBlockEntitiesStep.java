package org.minerift.ether.schematic.sponge.reader.steps;

import org.minerift.ether.Ether;
import org.minerift.ether.math.Vec3i;
import org.minerift.ether.nms.BlockStateNotFoundException;
import org.minerift.ether.schematic.SchematicReadException;
import org.minerift.ether.schematic.sponge.reader.SchematicReaderContext;
import org.minerift.ether.util.nbt.NbtException;
import org.minerift.ether.util.nbt.tags.IntTag;
import org.minerift.ether.util.nbt.tags.StringTag;
import org.minerift.ether.util.nbt.tags.TagTypes;
import org.minerift.ether.util.nbt.tags.container.*;
import org.minerift.ether.world.BlockEntityArchetype;
import org.slf4j.Logger;

import java.util.List;

import static org.minerift.ether.schematic.sponge.SpongeVersion.V1;
import static org.minerift.ether.schematic.sponge.reader.SchematicNBTFields.*;

public class ReadBlockEntitiesStep implements IReaderStep {

    private static final Logger LOGGER = Ether.inst().getLogger();

    @Override
    public void read(SchematicReaderContext ctx) throws SchematicReadException {

        final String blockEntitiesKey =
                ctx.builder.getVersion() == V1 ? NBT_TILE_ENTITIES : NBT_BLOCK_ENTITIES;

        final ListTag<CompoundTag> bEntities;
        try {
            bEntities = ctx.root.getList(blockEntitiesKey, TagTypes.COMPOUND);
        } catch (NoTagFoundException e) {
            // pass; no block entities
            LOGGER.debug("Read block entities: {}", ctx.builder.getBlocks().getRight().blockEntities);
            return;
        } catch (MismatchedTypeException | MismatchedChildTypeException e) {
            LOGGER.debug("Read block entities: {}", ctx.builder.getBlocks().getRight().blockEntities);
            return;
        }

        for(CompoundTag bEntityTag : bEntities) {
            final String id;
            final int[] rawPos;
            try {
                id = bEntityTag.getString(NBT_BLOCK_ENTITIES_ID,
                        (e) -> new NbtException("id", e));
                rawPos = bEntityTag.getIntArray(NBT_BLOCK_ENTITIES_POS,
                        (e) -> new NbtException("position", e));
            } catch (NbtException e) {
                LOGGER.warn("Failed to read block entity", e);
                continue;
            }
            final Vec3i pos = new Vec3i.Mutable(rawPos);

            // TODO: review nbt data fix up
            // Fix up nbt data
            CompoundTag nbt = bEntityTag.copy();
            nbt.addTag( new IntTag("x", pos.getX()) );
            nbt.addTag( new IntTag("y", pos.getY()) );
            nbt.addTag( new IntTag("z", pos.getZ()) );
            nbt.addTag( new StringTag("id", id) );

            nbt.removeTag(NBT_ENTITIES_POS);
            nbt.removeTag(NBT_ENTITIES_ID);

            BlockEntityArchetype bEntity;
            try {
                bEntity = new BlockEntityArchetype(id, pos, nbt);
                ctx.builder.getBlocks().getRight().addBlockEntity(bEntity);
            } catch (BlockStateNotFoundException ex1) {
                LOGGER.warn("BlockState for block entity not found, attempting fixup", ex1);
                // Fix up any outdated nbt data to try again
                try {
                    StringTag idTag = nbt.getTag("id", TagTypes.STRING);
                    idTag = Ether.inst().getNms().fixUpItemName(idTag, -1);
                    idTag.setName("id");
                    nbt.addTag(idTag, true);

                    bEntity = new BlockEntityArchetype(nbt.getTag("id", TagTypes.STRING).getStrVal(), pos, nbt);
                    ctx.builder.getBlocks().getRight().addBlockEntity(bEntity);
                } catch (BlockStateNotFoundException | NoTagFoundException | MismatchedTypeException ex2) {
                    // skip for now
                    LOGGER.error("Failed to read block entity", ex2);
                }
            }

        }

    }
}
