package org.minerift.ether.schematic.readers.sponge.steps;

import org.minerift.ether.schematic.SchematicFileReadException;
import org.minerift.ether.schematic.readers.sponge.ReaderContext;
import org.minerift.ether.util.math.Vec3d;
import org.minerift.ether.util.nbt.NBTSectionView;
import org.minerift.ether.util.nbt.tags.CompoundTag;
import org.minerift.ether.util.nbt.tags.Tag;
import org.minerift.ether.world.EntityArchetype;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.minerift.ether.schematic.readers.sponge.SchematicNBTFields.*;

public class ReadEntitiesStep implements IReaderStep {
    @Override
    public void read(ReaderContext ctx) throws SchematicFileReadException {

        final Optional<List<Tag>> tagList = ctx.rootView.getList(NBT_ENTITIES);

        if(tagList.isPresent()) {
            List<NBTSectionView> entitiesRaw = tagList.get().stream().map(tag -> new NBTSectionView((CompoundTag)tag)).toList();
            ctx.builder.setEntities(new HashSet<>(entitiesRaw.size()));

            for(NBTSectionView entity : entitiesRaw) {

                final String id = entity.getString(NBT_ENTITIES_ID).orElseThrow(() -> new SchematicFileReadException("Failed to read entity ids!"));
                final Double[] posRaw = entity.getDoubleArray(NBT_ENTITIES_POS).orElseThrow(() -> new SchematicFileReadException("Failed to read entity positions!"));
                final Vec3d.Mutable pos = new Vec3d.Mutable(posRaw[0], posRaw[1], posRaw[2]);

                // Fix up NBT data
                Map<String, Tag> rawNbt = entity.getSectionTags();
                rawNbt.remove("Id");
                rawNbt.remove("Pos");

                CompoundTag fixedNbt = new CompoundTag(entity.getName(), rawNbt);

                ctx.builder.getEntities().add(new EntityArchetype(id, pos, fixedNbt));
            }
        }
    }
}
