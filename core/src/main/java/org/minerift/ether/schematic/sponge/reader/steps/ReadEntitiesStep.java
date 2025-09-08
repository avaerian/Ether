package org.minerift.ether.schematic.sponge.reader.steps;

import org.minerift.ether.schematic.SchematicFileReadException;
import org.minerift.ether.schematic.sponge.reader.SchematicReaderContext;
import org.minerift.ether.math.Vec3d;
import org.minerift.ether.util.nbt.NbtException;
import org.minerift.ether.util.nbt.tags.Tag;
import org.minerift.ether.util.nbt.tags.container.*;
import org.minerift.ether.world.EntityArchetype;

import java.util.*;

import static org.minerift.ether.schematic.sponge.reader.SchematicNBTFields.*;
import static org.minerift.ether.util.nbt.tags.TagTypes.COMPOUND;
import static org.minerift.ether.util.nbt.tags.TagTypes.LIST;

public class ReadEntitiesStep implements IReaderStep {

    @Override
    public void read(SchematicReaderContext ctx) throws SchematicFileReadException {

        final ListTag<CompoundTag> tagList;
        try {
            tagList = ctx.root.getList(NBT_ENTITIES, COMPOUND);
        } catch (NoTagFoundException e) {
            // pass
            return;
        } catch (MismatchedTypeException | MismatchedChildTypeException e) {
            //final ListTag<?> tag = ctx.root.tryGetTag(NBT_ENTITIES, LIST);
            // TODO: log
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
                // TODO: logger
                System.out.println("Failed to read entity: " + e);
                continue;
            }

            final Vec3d.Mutable pos = new Vec3d.Mutable(posRaw[0], posRaw[1], posRaw[2]);

            // Fix up nbt data
            Map<String, Tag> rawNbt = entity.getValue();
            rawNbt.remove("Id");
            rawNbt.remove("Pos");

            CompoundTag fixedNbt = new CompoundTag(entity.getName(), rawNbt);
            ctx.builder.getEntities().add(new EntityArchetype(id, pos, fixedNbt));
        }
    }

}
