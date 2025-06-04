package org.minerift.ether.nms.v1_20_R2.data;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.bukkit.Material;
import org.minerift.ether.nms.BlockStateNotFoundException;
import org.minerift.ether.nms.v1_20_R2.NativeTypeConversionsImpl;
import org.minerift.ether.nms.world.BlockState;

import java.time.Duration;

public class BlockStateImpl implements BlockState<net.minecraft.world.level.block.state.BlockState> {

    private static final Cache<net.minecraft.world.level.block.state.BlockState, BlockStateImpl> CACHE;

    static {
        // TODO: review cache
        CACHE = CacheBuilder.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(1))
                .build();
    }

    public static BlockStateImpl of(String id) throws BlockStateNotFoundException {
        // TODO: review caching here
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
    public boolean canBeReplaced() {
        return state.canBeReplaced();
    }

    @Override
    public boolean isAir() {
        return state.isAir();
    }

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
}
