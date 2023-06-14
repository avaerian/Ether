package org.minerift.ether.generation;

import org.bukkit.*;
import org.minerift.ether.EtherPlugin;
import org.minerift.ether.island.Island;
import org.minerift.ether.nms.NMS;
import org.minerift.ether.user.EtherUser;

// Strategy for setting all blocks in a region to air for clearing islands
public class ClearIsland {

    public void clearIsland(Island island) {

        EtherUser owner = island.getOwner();
        World world = owner.getOfflinePlayer().getPlayer().getWorld();

        // Get island endpoints
        Chunk e1 = island.getTopLeftBound(world);
        Chunk e2 = island.getBottomRightBound(world);

        NMS nms = EtherPlugin.getInstance().getNMS();
        nms.clearChunksAsync(e1, e2, true);

    }

}
