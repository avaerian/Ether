package org.minerift.ether.schematic.sponge.reader.steps;

import org.minerift.ether.schematic.SchematicFileReadException;
import org.minerift.ether.schematic.sponge.reader.SchematicReaderContext;
import org.minerift.ether.math.Vec3d;
import org.minerift.ether.util.nbt.tags.Tag;
import org.minerift.ether.util.nbt.tags.container.CompoundTag;
import org.minerift.ether.util.nbt.tags.container.ListTag;
import org.minerift.ether.world.EntityArchetype;

import java.util.*;

import static org.minerift.ether.schematic.sponge.reader.SchematicNBTFields.*;

public class ReadEntitiesStep implements IReaderStep {

    @Override
    public void read(SchematicReaderContext ctx) throws SchematicFileReadException {

        // For getList(), we want to be able to get a ListTag<Tag<T>> and unwrap into List<T>
        final Optional<ListTag<CompoundTag>> tagList = ctx.root.getList(NBT_ENTITIES, CompoundTag.class);

        if(tagList.isPresent()) {
            List<CompoundTag> entitiesRaw = tagList.get().getValue();
            ctx.builder.setEntities(new ArrayList<>(entitiesRaw.size()));

            for(CompoundTag entity : entitiesRaw) {

                final String id = entity.getString(NBT_ENTITIES_ID).orElseThrow(() -> new SchematicFileReadException("Failed to read entity ids!"));
                final double[] posRaw = entity.getDoubleArray(NBT_ENTITIES_POS).orElseThrow(() -> new SchematicFileReadException("Failed to read entity positions!"));
                final Vec3d.Mutable pos = new Vec3d.Mutable(posRaw[0], posRaw[1], posRaw[2]);

                // Fix up NBT data
                Map<String, Tag> rawNbt = entity.getValue();
                rawNbt.remove("Id");
                rawNbt.remove("Pos");

                CompoundTag fixedNbt = new CompoundTag(entity.getName(), rawNbt);

                ctx.builder.getEntities().add(new EntityArchetype(id, pos, fixedNbt));
            }
        }
    }

}
