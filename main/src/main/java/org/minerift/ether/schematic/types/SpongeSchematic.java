package org.minerift.ether.schematic.types;

import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.minerift.ether.schematic.objects.SchematicBlockEntity;
import org.minerift.ether.schematic.objects.SchematicEntity;
import org.minerift.ether.world.QueuedBlock;

import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class SpongeSchematic {



    public static class Builder {

        // sponge schematic version (differs from DataVersion; refers to Minecraft version)
        protected int version;
        protected short width, height, length;

        protected BlockData[][][] blocks;

        protected Set<QueuedBlock> blocksNew;

        protected String[][] biomes;

        protected Set<SchematicBlockEntity> blockEntities;
        protected Set<SchematicEntity> entities;



        // GETTERS

        public int getVersion() {
            return version;
        }

        public short getWidth() {
            return width;
        }

        public short getHeight() {
            return height;
        }

        public short getLength() {
            return length;
        }

        public BlockData[][][] getBlocks() {
            return blocks;
        }

        public Set<QueuedBlock> getBlocksNew() {
            return blocksNew;
        }

        public String[][] getBiomes() {
            return biomes;
        }

        public Set<SchematicBlockEntity> getBlockEntities() {
            return blockEntities;
        }

        public Set<SchematicEntity> getEntities() {
            return entities;
        }


        // SETTERS

        public void setVersion(int version) {
            this.version = version;
        }

        public void setWidth(short width) {
            this.width = width;
        }

        public void setHeight(short height) {
            this.height = height;
        }

        public void setLength(short length) {
            this.length = length;
        }

        public void setBlocks(BlockData[][][] blocks) {
            this.blocks = blocks;
        }

        public void setBlocksNew(Set<QueuedBlock> blocksNew) {
            this.blocksNew = blocksNew;
        }

        public void setBiomes(String[][] biomes) {
            this.biomes = biomes;
        }

        public void setBlockEntities(Set<SchematicBlockEntity> blockEntities) {
            this.blockEntities = blockEntities;
        }

        public void setEntities(Set<SchematicEntity> entities) {
            this.entities = entities;
        }
    }
}
