package org.minerift.ether.nms.v1_20_R2.data;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.bukkit.NamespacedKey;
import org.minerift.ether.nms.BiomeNotFoundException;
import org.minerift.ether.nms.v1_20_R2.NativeTypeConversionsImpl;
import org.minerift.ether.nms.world.Biome;

import java.util.HashMap;
import java.util.Map;

public class BiomeImpl implements Biome<Holder<net.minecraft.world.level.biome.Biome>> {

    private static final Map<String, ResourceKey<net.minecraft.world.level.biome.Biome>> RESOURCE_CACHE;
    private static final Map<ResourceKey<net.minecraft.world.level.biome.Biome>, BiomeImpl> CACHE;

    static {
        RESOURCE_CACHE = new HashMap<>();
        CACHE = new HashMap<>();
    }

    public static BiomeImpl of(ResourceKey<net.minecraft.world.level.biome.Biome> biomeKey) throws BiomeNotFoundException {
        BiomeImpl biome = CACHE.get(biomeKey);
        if(biome == null) {
            biome = new BiomeImpl(NativeTypeConversionsImpl.inst().asNativeBiome(biomeKey));
            RESOURCE_CACHE.put(biomeKey.location().toString(), biomeKey);
            CACHE.put(biomeKey, biome);
        }
        return biome;
    }

    public static BiomeImpl of(String id) throws BiomeNotFoundException {
        ResourceKey<net.minecraft.world.level.biome.Biome> biomeKey = RESOURCE_CACHE.get(id);
        if(biomeKey == null) {
            biomeKey = ResourceKey.create(Registries.BIOME, new ResourceLocation(id));
        }
        return of(biomeKey);
    }

    private final Holder.Reference<net.minecraft.world.level.biome.Biome> biome;

    private BiomeImpl(Holder.Reference<net.minecraft.world.level.biome.Biome> nativeBiome) {
        this.biome = nativeBiome;
    }

    @Override
    public String getResourceKey() {
        return biome.key().toString();
    }

    @Override
    public NamespacedKey getNamespacedKey() {
        return NamespacedKey.fromString(getResourceKey());
    }

    @Override
    public boolean hasPrecipitation() {
        return biome.value().hasPrecipitation();
    }

    @Override
    public int getFogColor() {
        return biome.value().getFogColor();
    }

    @Override
    public int getFoliageColor() {
        return biome.value().getFoliageColor();
    }

    @Override
    public int getWaterColor() {
        return biome.value().getWaterColor();
    }

    @Override
    public NativeTypeConversionsImpl getConverter() {
        return NativeTypeConversionsImpl.inst();
    }

    @Override
    public Holder<net.minecraft.world.level.biome.Biome> asNative() {
        return biome;
    }
}
