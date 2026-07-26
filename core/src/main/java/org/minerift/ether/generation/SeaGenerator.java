package org.minerift.ether.generation;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.bukkit.util.noise.SimplexOctaveGenerator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.minerift.ether.nms.BlockStateNotFoundException;
import org.minerift.ether.nms.world.Chunk;
import org.minerift.ether.nms.world.ChunkGetter;
import org.minerift.ether.nms.world.block.BlockState;

import java.util.List;
import java.util.Random;

public class SeaGenerator extends ChunkGenerator {

    private BiomeProvider biomeProvider;
    private int sandHeight;
    private int waterHeight;

    public SeaGenerator(int sandHeight, int waterHeight) {
        this.biomeProvider = new VoidBiomeProvider();
        this.sandHeight = sandHeight;
        this.waterHeight = waterHeight;
    }

    @Override
    public @NotNull ChunkData createVanillaChunkData(@NotNull World world, int cx, int cz) {
        ChunkData data = Bukkit.createChunkData(world);
        // we can create the ChunkData and instead use our own NMS Chunk access to apply the changes we want
        // TODO: review this
        Chunk chunk = ChunkGetter.SYNC.getChunk(world, cx, cz).join();
        try {
            BlockState sand = BlockState.of("minecraft:sand");
            BlockState water = BlockState.of("minecraft:water");
            BlockState bedrock = BlockState.of("minecraft:bedrock");

            for(int z = 0; z < 16; z++) {
                for(int x = 0; x < 16; x++) {
                    chunk.setBlockState(x, 0, z, bedrock);
                }
            }

            for(int z = 0; z < 16; z++) {
                for(int y = 0; y < sandHeight; y++) {
                    for(int x = 0; x < 16; x++) {
                        chunk.setBlockState(x, y, z, sand);
                    }
                }
            }
        } catch (BlockStateNotFoundException e) {
            throw new RuntimeException(e);
        }

        return super.createVanillaChunkData(world, cx, cz);
    }

    @Override
    public void generateNoise(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull ChunkData chunkData) {
        super.generateNoise(worldInfo, random, chunkX, chunkZ, chunkData);
    }

    @Override
    public void generateSurface(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull ChunkData chunkData) {
        super.generateSurface(worldInfo, random, chunkX, chunkZ, chunkData);
    }

    @Override
    public void generateCaves(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull ChunkData chunkData) {
        super.generateCaves(worldInfo, random, chunkX, chunkZ, chunkData);
    }

    @Override
    public @Nullable BiomeProvider getDefaultBiomeProvider(@NotNull WorldInfo worldInfo) {
        return biomeProvider;
    }

    @Override
    public boolean shouldGenerateNoise() {
        return super.shouldGenerateNoise();
    }

    @Override
    public boolean shouldGenerateCaves() {
        return super.shouldGenerateCaves();
    }

    @Override
    public boolean shouldGenerateDecorations() {
        return super.shouldGenerateDecorations();
    }

    @Override
    public boolean shouldGenerateSurface() {
        return super.shouldGenerateSurface();
    }

    @Override
    public boolean shouldGenerateMobs() {
        return super.shouldGenerateMobs();
    }

    @Override
    public boolean shouldGenerateStructures() {
        return super.shouldGenerateStructures();
    }

    @Override
    public @NotNull List<BlockPopulator> getDefaultPopulators(@NotNull World world) {
        return super.getDefaultPopulators(world);
    }
}
