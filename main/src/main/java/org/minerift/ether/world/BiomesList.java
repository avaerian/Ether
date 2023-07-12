package org.minerift.ether.world;

import org.bukkit.NamespacedKey;
import org.bukkit.block.Biome;

import java.util.HashMap;
import java.util.Map;

// Biomes list based on namespaced keys
public class BiomesList {

    private static final Map<NamespacedKey, Biome> BIOMES = new HashMap<>();

    static {
        for(Biome biome : Biome.values()) {
            if(biome != Biome.CUSTOM) {
                BIOMES.put(biome.getKey(), biome);
            }
        }
    }

    public static Biome getBiome(String key) {
        return getBiome(NamespacedKey.fromString(key));
    }

    public static Biome getBiome(NamespacedKey key) {
        return BIOMES.get(key);
    }

}
