package org.minerift.ether.nms.v1_20_R2.data;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.world.level.block.state.properties.Property;
import org.bukkit.Material;
import org.minerift.ether.debug.NeedsTesting;
import org.minerift.ether.nms.BlockStateNotFoundException;
import org.minerift.ether.nms.world.block.Attribute;
import org.minerift.ether.nms.v1_20_R2.NativeTypeConversionsImpl;
import org.minerift.ether.nms.world.block.BlockState;
import org.minerift.ether.util.nbt.tags.StringTag;
import org.minerift.ether.util.nbt.tags.container.CompoundTag;

import java.time.Duration;
import java.util.Map;

public class BlockStateImpl implements BlockState<net.minecraft.world.level.block.state.BlockState> {

    // TODO: review caching; move away from it in favor of Property's
    private static final Cache<net.minecraft.world.level.block.state.BlockState, BlockStateImpl> CACHE;

    static {
        CACHE = CacheBuilder.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(1))
                .build();
    }

    public static BlockStateImpl of(String id) throws BlockStateNotFoundException {
        net.minecraft.world.level.block.state.BlockState nativeState
                = NativeTypeConversionsImpl.inst().asNativeBlockState(id);
        System.out.println(id + " " + nativeState.toString());
        return of(nativeState);
    }

    public static BlockStateImpl of(net.minecraft.world.level.block.state.BlockState nativeState) {
        BlockStateImpl state = CACHE.getIfPresent(nativeState);
        if(state == null) {
            state = new BlockStateImpl(nativeState);
            CACHE.put(nativeState, state);
        }
        return state;
    }

    private final net.minecraft.world.level.block.state.BlockState state;

    public BlockStateImpl(net.minecraft.world.level.block.state.BlockState state) {
        this.state = state;
    }

    @Override
    public boolean hasBlockEntity() {
        return state.hasBlockEntity();
    }

    @Override
    public boolean hasAttribute(Attribute<?> attr) {
        return AttributeRegistry.access().hasAttr(state, attr);
    }

    @Override
    public <T> T getAttribute(Attribute<T> attr) {
        return AttributeRegistry.access().getValue(state, attr);
    }

    @Override
    public <T> T tryGetAttribute(Attribute<T> attr) {
        return AttributeRegistry.access().tryGetValue(state, attr);
    }

    @Override
    public <T> BlockState<net.minecraft.world.level.block.state.BlockState> setAttribute(Attribute<T> attr, T val) {
        net.minecraft.world.level.block.state.BlockState nState =
                AttributeRegistry.access().setValue(state, attr, val);
        return BlockStateImpl.of(nState); // TODO: review
    }

    @Override
    public <T> BlockState<net.minecraft.world.level.block.state.BlockState> trySetAttribute(Attribute<T> attr, T val) {
        net.minecraft.world.level.block.state.BlockState nState =
                AttributeRegistry.access().trySetValue(state, attr, val);
        return BlockStateImpl.of(nState);
    }

    // may be able to cache this if needed
    @NeedsTesting
    @Override
    public CompoundTag propsAsNbt() {
        CompoundTag compound = new CompoundTag();

        for(Map.Entry<Property<?>, Comparable<?>> entry : state.getValues().entrySet()) {
            Property prop = entry.getKey();
            compound.addTag(new StringTag(prop.getName(), prop.getName(entry.getValue())));
        }

        return compound;
    }

    @Override
    public boolean canBeReplaced() {
        return state.canBeReplaced();
    }

    @Override
    public boolean isAir() {
        return state.isAir();
    }

    @Deprecated
    @Override
    public Material getBukkitMaterial() {
        return state.getBukkitMaterial();
    }

    @Override
    public net.minecraft.world.level.block.state.BlockState asNative() {
        return state;
    }

    @Override
    public NativeTypeConversionsImpl getConverter() {
        return NativeTypeConversionsImpl.inst();
    }

    /*@Override
    public BlockState<net.minecraft.world.level.block.state.BlockState> copy() {
        // FIXME
        return new BlockStateImpl(new net.minecraft.world.level.block.state.BlockState(state.getBlock(), state.getValues(), );
    }*/
}
