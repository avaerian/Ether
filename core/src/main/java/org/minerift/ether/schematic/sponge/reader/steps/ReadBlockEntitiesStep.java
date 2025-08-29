package org.minerift.ether.schematic.sponge.reader.steps;

import org.minerift.ether.Ether;
import org.minerift.ether.math.Vec3i;
import org.minerift.ether.nms.BlockStateNotFoundException;
import org.minerift.ether.schematic.SchematicFileReadException;
import org.minerift.ether.schematic.sponge.reader.SchematicReaderContext;
import org.minerift.ether.util.nbt.tags.IntTag;
import org.minerift.ether.util.nbt.tags.StringTag;
import org.minerift.ether.util.nbt.tags.TagTypes;
import org.minerift.ether.util.nbt.tags.container.CompoundTag;
import org.minerift.ether.util.nbt.tags.container.ListTag;
import org.minerift.ether.world.BlockEntityArchetype;

import java.util.List;
import java.util.Optional;

import static org.minerift.ether.schematic.sponge.SpongeVersion.V1;
import static org.minerift.ether.schematic.sponge.reader.SchematicNBTFields.*;

public class ReadBlockEntitiesStep implements IReaderStep {
    @Override
    public void read(SchematicReaderContext ctx) throws SchematicFileReadException {

        final String blockEntitiesKey =
                ctx.builder.getVersion() == V1 ? NBT_TILE_ENTITIES : NBT_BLOCK_ENTITIES;
        final Optional<ListTag<CompoundTag>> tagList = ctx.root.getList(blockEntitiesKey, TagTypes.COMPOUND);

        //Vec3i dim = ctx.builder.getDimensions();

        if(tagList.isPresent()) {

            List<CompoundTag> blockEntitiesRaw = tagList.get().getValue();
            for(CompoundTag bEntityTag : blockEntitiesRaw) {

                final String id = bEntityTag.getString(NBT_BLOCK_ENTITIES_ID).orElseThrow(() -> new SchematicFileReadException("Failed to read block entity ids!"));
                final int[] rawPos = bEntityTag.getIntArray(NBT_BLOCK_ENTITIES_POS).orElseThrow(() -> new SchematicFileReadException("Failed to read block entity positions!"));
                final Vec3i pos = new Vec3i.Mutable(rawPos);

                //System.out.println(bEntityTag);

                // TODO: review nbt data fix up
                // Fix up NBT data
                CompoundTag nbt = bEntityTag.copy();
                nbt.addTag( new IntTag("x", pos.getX()) );
                nbt.addTag( new IntTag("y", pos.getY()) );
                nbt.addTag( new IntTag("z", pos.getZ()) );
                nbt.addTag( new StringTag("id", id) );

                //ListTag<StringTag> test = bEntityTag.getListTag("test", TagType.STRING);

                nbt.removeTag(NBT_ENTITIES_POS);
                nbt.removeTag(NBT_ENTITIES_ID);

                //int idx = Array3DOrder.YZX.flatten(dim.getX(), dim.getZ(), pos.getX(), pos.getY(), pos.getZ());
                //System.out.println("first: " + nbt);
                BlockEntityArchetype bEntity;
                try {
                    bEntity = new BlockEntityArchetype(id, pos, nbt);
                    ctx.builder.getBlocks().addBlockEntity(bEntity);
                } catch (BlockStateNotFoundException ex1) {
                    // TODO: logger
                    // Fix up any outdated nbt data to try again
                    StringTag idTag = nbt.getTag("id", TagTypes.STRING);
                    idTag = Ether.getNms().fixUpItemName(idTag, -1);
                    idTag.setName("id");
                    nbt.addTag(idTag, true);
                    //System.out.println("second: " + nbt);
                    try {
                        bEntity = new BlockEntityArchetype(nbt.getTag("id", TagTypes.STRING).getValue(), pos, nbt);
                        ctx.builder.getBlocks().addBlockEntity(bEntity);
                    } catch (BlockStateNotFoundException ex2) {
                        // skip for now
                        System.out.println("Failed to read block entity: ");
                        ex1.printStackTrace();
                        continue;
                    }
                }
            }
        }

        System.out.println("Read block entities: " + ctx.builder.getBlocks().blockEntities);
    }
}
