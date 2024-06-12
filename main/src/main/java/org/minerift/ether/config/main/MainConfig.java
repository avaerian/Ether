package org.minerift.ether.config.main;

import org.minerift.ether.config.Config;
import org.minerift.ether.config.ConfigType;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkArgument;

public class MainConfig extends Config<MainConfig> {

    public static final int CHUNK_SIZE = 16;
    public static final int MIN_TILE_CHUNKS = 3;
    public static final int MIN_TILE_SIZE = MIN_TILE_CHUNKS * CHUNK_SIZE; // 3 chunks * 16 blocks/chunk = 48 blocks

    private int tileLengthChunks;
    private int tileHeight;

    // I plan on adding permissions to this and allowing for different tiers
    private int tileAccessibleAreaBlocks;

    // This will be in milliseconds
    private long inviteInvalidateAfter;

    // Default values for config
    public MainConfig() {
        setTileLengthChunks(9); // default value for now
        setTileHeight(90);
        setTileAccessibleAreaBlocks(180); // default value for now; this is subject to change
        setInviteInvalidateAfter(TimeUnit.MINUTES.toMillis(2));
        setChanged(false);
    }

    // Getters
    public int getTileLengthChunks() {
        return tileLengthChunks;
    }

    public int getTileLengthBlocks() {
        return tileLengthChunks * CHUNK_SIZE;
    }

    public int getTileHeight() {
        return tileHeight;
    }

    public int getTileAccessibleAreaBlocks() {
        return tileAccessibleAreaBlocks;
    }

    public long getInviteInvalidateAfter() {
        return inviteInvalidateAfter;
    }

    // Setters

    public void setInviteInvalidateAfter(long ms) {
        this.inviteInvalidateAfter = ms;
        setChanged(true);
    }

    public void setTileLengthChunks(int tileLengthChunks) {
        this.tileLengthChunks = tileLengthChunks;
        setChanged(true);
    }

    /*
    @Deprecated(forRemoval = true)
    // Don't use this function. The variables are all wrong so it won't work properly.
    public void setTileSizeBlocks(int tileSize) {
        // Round the tile size down to chunks (multiples of 16)
        final int tileSizeActual = tileSize - (tileSize % CHUNK_SIZE);
        checkArgument(tileSizeActual >= MIN_TILE_SIZE, String.format("Tile size (%d -> %d) cannot be below min size %d!", tileSize, tileSizeActual, MIN_TILE_SIZE));

        this.tileLengthChunks = tileSizeActual;

        // Update accessible region if bigger than new tile size
        if(tileAccessibleArea > tileSizeActual) {
            tileAccessibleArea = tileSizeActual;
        }

        setChanged(true);
    }*/

    public void setTileHeight(int tileHeight) {
        this.tileHeight = tileHeight;
        setChanged(true);
    }

    public void setTileAccessibleAreaBlocks(int tileAccessibleAreaBlocks) {
        this.tileAccessibleAreaBlocks = tileAccessibleAreaBlocks;
        setChanged(true);
    }

    @Override
    protected void copyFrom(MainConfig other) {
        if(!other.equals(this)) {
            this.tileLengthChunks = other.tileLengthChunks;
            this.tileHeight = other.tileHeight;
            this.tileAccessibleAreaBlocks = other.tileAccessibleAreaBlocks;
            this.inviteInvalidateAfter = other.inviteInvalidateAfter;
            setChanged(true);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MainConfig that = (MainConfig) o;
        return tileLengthChunks == that.tileLengthChunks
                && tileHeight == that.tileHeight
                && tileAccessibleAreaBlocks == that.tileAccessibleAreaBlocks
                && inviteInvalidateAfter == that.inviteInvalidateAfter;
    }

    @Override
    public int hashCode() {
        return Objects.hash(tileLengthChunks, tileHeight, tileAccessibleAreaBlocks, inviteInvalidateAfter);
    }

    @Override
    public ConfigType<MainConfig> getType() {
        return ConfigType.MAIN;
    }
}
