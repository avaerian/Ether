package org.minerift.ether.world;

public interface WorldAccess {

    // Is chunk absent of all blocks except air
    boolean isChunkClear();

    void clearChunk();

    void clearChunks();

    // Return a map of blocks and block counts and stuff
    void scanChunk();

    void setBlocks();

    void setBlockAsync();

    void spawnEntity();

    void getWorldType();

}
