package org.minerift.ether.schematic.sponge.reader.steps;

import com.google.common.collect.Maps;
import org.minerift.ether.math.Vec3i;
import org.minerift.ether.schematic.SchematicFileReadException;
import org.minerift.ether.schematic.sponge.reader.SchematicReaderContext;
import org.minerift.ether.util.nunbt.tags.IntTag;
import org.minerift.ether.util.nunbt.tags.StringTag;
import org.minerift.ether.util.nunbt.tags.Tag;
import org.minerift.ether.util.nunbt.tags.container.CompoundTag;
import org.minerift.ether.util.nunbt.tags.container.ListTag;
import org.minerift.ether.world.BlockEntityArchetype;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.minerift.ether.schematic.sponge.SpongeVersion.V1;
import static org.minerift.ether.schematic.sponge.reader.SchematicNBTFields.*;

public class ReadBlockEntitiesStep implements IReaderStep {
    @Override
    public void read(SchematicReaderContext ctx) throws SchematicFileReadException {

        final String blockEntitiesKey =
                ctx.builder.getVersion() == V1 ? NBT_TILE_ENTITIES : NBT_BLOCK_ENTITIES;
        final Optional<ListTag<CompoundTag>> tagList = ctx.root.getList(blockEntitiesKey);

        //Vec3i dim = ctx.builder.getDimensions();

        if(tagList.isPresent()) {

            List<CompoundTag> blockEntitiesRaw = tagList.get().getValue();
            for(CompoundTag bEntityTag : blockEntitiesRaw) {

                final String id = bEntityTag.getString(NBT_BLOCK_ENTITIES_ID).orElseThrow(() -> new SchematicFileReadException("Failed to read block entity ids!"));
                final int[] rawPos = bEntityTag.getIntArray(NBT_BLOCK_ENTITIES_POS).orElseThrow(() -> new SchematicFileReadException("Failed to read block entity positions!"));
                final Vec3i pos = new Vec3i.Mutable(rawPos);

                // Fix up NBT data
                Map<String, Tag<?>> rawNbt = Maps.newHashMap(bEntityTag.getValue());

                rawNbt.put("x", new IntTag("x", pos.getX()));
                rawNbt.put("y", new IntTag("y", pos.getY()));
                rawNbt.put("z", new IntTag("z", pos.getZ()));
                rawNbt.put("id", new StringTag("id", id));

                rawNbt.remove(NBT_ENTITIES_POS);
                rawNbt.remove(NBT_ENTITIES_ID);

                CompoundTag fixedNbt = new CompoundTag(bEntityTag.getName(), rawNbt);

                //int idx = Array3DOrder.YZX.flatten(dim.getX(), dim.getZ(), pos.getX(), pos.getY(), pos.getZ());
                BlockEntityArchetype bEntity = new BlockEntityArchetype(id, pos, fixedNbt);
                ctx.builder.getBlocks().addBlockEntity(bEntity);
            }
        }
    }
}
