package org.minerift.ether.nms.v1_20_R2;

import com.mojang.serialization.Dynamic;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.bukkit.World;
import org.minerift.ether.nms.NMSAccess;
import org.minerift.ether.nms.v1_20_R2.data.AttributeRegistry;
import org.minerift.ether.util.fixer.NbtFixerOps;
import org.minerift.ether.util.nbt.tags.Tag;
import org.minerift.ether.world.EntityArchetype;
import org.minerift.ether.world.EntityLoadException;

public class NMSAccessImpl implements NMSAccess {

    private final RegistryAccessImpl registryAccess;
    private final AttributeRegistry attrRegistry;

    public NMSAccessImpl() {
        this.registryAccess = new RegistryAccessImpl();
        this.attrRegistry = AttributeRegistry.access();
    }

    @Override
    public <T extends Tag> T fixUpItemName(T nbt, int dataVersion) {
        net.minecraft.nbt.Tag nativeNbt = getConverter().asNativeTag(nbt);
        Dynamic<net.minecraft.nbt.Tag> name = new Dynamic<>(NbtOps.INSTANCE, nativeNbt);
        Dynamic<net.minecraft.nbt.Tag> converted = DataFixers.getDataFixer().update(References.ITEM_NAME, name, dataVersion, getDataVersion());
        if(name.equals(converted)) {
            converted = DataFixers.getDataFixer().update(References.BLOCK_NAME, name, dataVersion, getDataVersion());
        }

        return (T) getConverter().asTag(converted.cast(NbtOps.INSTANCE));
    }

    // TODO: test this after finishing NbtFixerOps.class
    // TODO: refactor to move out of NMS into core
    /*@Override
    public <T extends Tag> T fixUpItemName(T nbt, int dataVersion) {
        Dynamic<Tag> name = new Dynamic<>(NbtFixerOps.INSTANCE, nbt);
        Dynamic<Tag> converted = DataFixers.getDataFixer().update(References.ITEM_NAME, name, dataVersion, getDataVersion());
        if(name.equals(converted)) {
            converted = DataFixers.getDataFixer().update(References.BLOCK_NAME, name, dataVersion, getDataVersion());
        }

        return (T) converted.cast(NbtFixerOps.INSTANCE);
    }*/

    @Override
    public void addEntity(World world, EntityArchetype entity) throws EntityLoadException {
        ServerLevel level = getConverter().asNativeWorld(world);
        CompoundTag nativeTag = (CompoundTag) getConverter().asNativeTag(entity.getNbtData());
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
    public int getDataVersion() {
        return SharedConstants.getCurrentVersion().getDataVersion().getVersion();
    }

    @Override
    public RegistryAccessImpl registryAccess() {
        //((CraftItemStack)((ItemStack)null)).handle.
        return registryAccess;
    }

    public AttributeRegistry attrRegistry() {
        return AttributeRegistry.access();
    }

    @Override
    public NativeTypeConversionsImpl getConverter() {
        return NativeTypeConversionsImpl.inst();
    }
}
