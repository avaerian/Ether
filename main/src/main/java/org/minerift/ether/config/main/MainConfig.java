package org.minerift.ether.config.main;

import org.minerift.ether.config.Config;
import org.minerift.ether.config.ConfigType;

import static com.google.common.base.Preconditions.checkArgument;

public class MainConfig extends Config<MainConfig> {

    public static final int CHUNK_SIZE = 16;
    public static final int MIN_TILE_SIZE = 3 * CHUNK_SIZE; // 3 chunks * 16 blocks/chunk = 48 blocks

    private int tileSize;
    private int tileHeight;

    // I plan on adding permissions to this and allowing for different tiers
    private int tileAccessibleArea;

    public MainConfig() {
        setTileSize(200); // default value for now
        setTileHeight(90);
        setTileAccessibleArea(180); // default value for now; this is subject to change
        setChanged(false);
    }

    // Getters

    public int getTileSize() {
        return tileSize;
    }

    public int getTileHeight() {
        return tileHeight;
    }

    public int getTileAccessibleArea() {
        return tileAccessibleArea;
    }

    // Setters

    public void setTileSize(int tileSize) {
        // Round the tile size to chunks
        final int tileSizeActual = tileSize - (tileSize % CHUNK_SIZE);
        checkArgument(tileSizeActual >= MIN_TILE_SIZE, String.format("Tile size (%d -> %d) cannot be below min size %d!", tileSize, tileSizeActual, MIN_TILE_SIZE));

        this.tileSize = tileSizeActual;

        // Update accessible region if bigger than new tile size
        if(tileAccessibleArea > tileSizeActual) {
            tileAccessibleArea = tileSizeActual;
        }

        setChanged(true);
    }

    public void setTileHeight(int tileHeight) {
        this.tileHeight = tileHeight;
        setChanged(true);
    }

    public void setTileAccessibleArea(int tileAccessibleArea) {
        this.tileAccessibleArea = tileAccessibleArea;
        setChanged(true);
    }

    @Override
    protected void copyFrom(MainConfig other) {
        this.tileSize = other.tileSize;
        this.tileHeight = other.tileHeight;
        this.tileAccessibleArea = other.tileAccessibleArea;
        setChanged(true);
    }

    @Override
    public ConfigType<MainConfig> getType() {
        return ConfigType.MAIN;
    }
}
