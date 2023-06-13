package org.minerift.ether.nms;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;

@Deprecated
public interface DeprecatedNMSBridge {

    // Clears a chunk of all blocks completely
    void fastClearChunk(Chunk chunk);

    // Regenerates a chunk (reset back to world gen state)
    void fastResetChunk(Chunk chunk);

    void fastClearRegion(Location p1, Location p2);

    void fastSetBlock(Location loc, Block block);

}
