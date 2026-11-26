package org.minerift.ether.schematic.sponge.reader.steps;

import org.minerift.ether.Ether;
import org.minerift.ether.math.Vec2d;
import org.minerift.ether.schematic.SchematicReadException;
import org.minerift.ether.schematic.sponge.reader.SchematicReaderContext;
import org.minerift.ether.math.Vec3d;
import org.minerift.ether.util.nbt.NbtException;
import org.minerift.ether.util.nbt.nunbt.DoubleArrayNuTag;
import org.minerift.ether.util.nbt.tags.DoubleTag;
import org.minerift.ether.util.nbt.tags.container.*;
import org.minerift.ether.world.EntityArchetype;
import org.minerift.ether.world.Location;
import org.slf4j.Logger;

import java.util.*;

import static org.minerift.ether.schematic.sponge.reader.SchematicNBTFields.*;
import static org.minerift.ether.util.nbt.tags.TagTypes.COMPOUND;
import static org.minerift.ether.util.nbt.tags.TagTypes.DOUBLE;

public class ReadEntitiesStep implements IReaderStep {

    public static final Logger LOGGER = Ether.inst().getLogger();

    @Override
    public void read(SchematicReaderContext ctx) throws SchematicReadException {

        final ListTag<CompoundTag> tagList;
        try {
            tagList = ctx.root.getList(NBT_ENTITIES, COMPOUND);
        } catch (NoTagFoundException e) {
            // pass
            return;
        } catch (MismatchedTypeException | MismatchedChildTypeException e) {
            LOGGER.error("Mismatched type or child type", e);
            return;
        }

        List<CompoundTag> entitiesRaw = tagList.getValue();
        ctx.builder.setEntities(new ArrayList<>(entitiesRaw.size()));

        for(CompoundTag entity : entitiesRaw) {

            final String id;
            final double[] posRaw;
            try {
                id = entity.getString(NBT_ENTITIES_ID, (e) -> new NbtException("id", e));
                posRaw = entity.getDoubleArray(NBT_ENTITIES_POS, (e) -> new NbtException("position", e));
            } catch (NbtException e) {
                LOGGER.error("Failed to read entity", e);
                continue;
            }

            final Vec3d.Mutable pos = new Vec3d.Mutable(posRaw[0], posRaw[1], posRaw[2]);

            // Fix up nbt data
            CompoundTag fixedNbt = entity.copy();
            fixedNbt.removeTag("Id");
            fixedNbt.removeTag("Pos");

            ListTag<DoubleTag> lookTag = null;
            CompoundTag data = entity.tryGetTag("Data", COMPOUND);
            if(data == null) {
                lookTag = data.tryGetList("Rotation", DOUBLE);
            }
            Vec2d look = lookTag == null ? Vec2d.ZERO : new Vec2d(DoubleArrayNuTag.Codec.tagsToDoubles(lookTag.getValue()));

            ctx.builder.getEntities().add(new EntityArchetype(id, new Location.Mutable(pos, look), fixedNbt));
        }
    }

}
