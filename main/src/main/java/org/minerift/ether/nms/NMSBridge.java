package org.minerift.ether.nms;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;

public interface NMSBridge {

    void fastClearChunk(Chunk chunk);

    void fastSetBlock(Location loc, Block block);


}
