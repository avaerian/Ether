package org.minerift.ether.nms.v1_20_R2;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.bukkit.World;
import org.minerift.ether.nms.NMSAccess;
import org.minerift.ether.world.EntityArchetype;
import org.minerift.ether.world.EntityLoadException;

public class NMSAccessImpl implements NMSAccess {

    private final RegistryAccessImpl registryAccess;

    public NMSAccessImpl() {
        this.registryAccess = new RegistryAccessImpl();
    }

    @Override
    public void addEntity(World world, EntityArchetype entity) throws EntityLoadException {
        ServerLevel level = getConverter().asNativeWorld(world);
        net.minecraft.nbt.CompoundTag nativeTag = (net.minecraft.nbt.CompoundTag) getConverter().asNativeTag(entity.getNbtData());
        Entity worldEntity = EntityType.loadEntityRecursive(nativeTag, level, (entity1) -> {
            entity1.moveTo(entity.getPos().getXd(), entity.getPos().getYd(), entity.getPos().getZd());
            return entity1;
        });
        if(worldEntity == null) {
            throw new EntityLoadException("Entity archetype (" + entity.getType() + ") failed to load: invalid type");
        }
        if(!level.tryAddFreshEntityWithPassengers(worldEntity)) {
            throw new EntityLoadException("Entity failed to add to world: duplicate UUID " + worldEntity.getStringUUID());
        }
        System.out.println("Added entity " + entity.getType() + " at " + entity.getPos()); // debug
    }

    @Override
    public RegistryAccessImpl registryAccess() {
        return registryAccess;
    }

    @Override
    public NativeTypeConversionsImpl getConverter() {
        return NativeTypeConversionsImpl.inst();
    }
}
