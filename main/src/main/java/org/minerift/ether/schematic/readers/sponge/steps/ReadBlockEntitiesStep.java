package org.minerift.ether.schematic.readers.sponge.steps;

import com.google.common.collect.Maps;
import org.minerift.ether.schematic.SchematicFileReadException;
import org.minerift.ether.schematic.readers.sponge.ReaderContext;
import org.minerift.ether.util.math.Vec3i;
import org.minerift.ether.util.nbt.NBTSectionView;
import org.minerift.ether.util.nbt.tags.CompoundTag;
import org.minerift.ether.util.nbt.tags.IntTag;
import org.minerift.ether.util.nbt.tags.StringTag;
import org.minerift.ether.util.nbt.tags.Tag;
import org.minerift.ether.world.BlockEntityArchetype;

import java.util.*;

import static org.minerift.ether.schematic.readers.sponge.SchematicNBTFields.*;

public class ReadBlockEntitiesStep implements IReaderStep {
    @Override
    public void read(ReaderContext ctx) throws SchematicFileReadException {

        final String blockEntitiesKey = ctx.builder.getVersion() == 1 ? NBT_TILE_ENTITIES : NBT_BLOCK_ENTITIES;
        final Optional<List<Tag>> tagList = ctx.rootView.getList(blockEntitiesKey);

        if(tagList.isPresent()) {

            List<NBTSectionView> blockEntitiesRaw = tagList.get().stream().map(tag -> new NBTSectionView((CompoundTag)tag)).toList();
            ctx.builder.setBlockEntities(new HashMap<>(blockEntitiesRaw.size()));
            for(NBTSectionView blockEntity : blockEntitiesRaw) {

                final String id = blockEntity.getString(NBT_BLOCK_ENTITIES_ID).orElseThrow(() -> new SchematicFileReadException("Failed to read block entity ids!"));
                final int[] rawPos = blockEntity.getIntArray(NBT_BLOCK_ENTITIES_POS).orElseThrow(() -> new SchematicFileReadException("Failed to read block entity positions!"));
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

                ctx.builder.getBlockEntities().put(pos, new BlockEntityArchetype(id, pos, fixedNbt));
            }
        }
    }
}
