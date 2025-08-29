package org.minerift.ether.debug;

import org.bukkit.Location;
import org.minerift.ether.nms.world.ChunkGetter;

// Expose test features to core
@Experimental
public interface Experiments {

    void testIslandScanIdea(Location loc);

    void testIslandScanIdeaFullChunk(Location loc);

    void testIslandScanIdeaMultiChunk(ChunkGetter cg, Location loc, int diameter);

}
