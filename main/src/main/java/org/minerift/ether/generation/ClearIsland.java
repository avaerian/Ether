package org.minerift.ether.generation;

import org.bukkit.Location;
import org.bukkit.World;
import org.minerift.ether.island.Island;

// Strategy for setting all blocks in a region to air for clearing islands
public class ClearIsland {

    public void clearIsland(Island island) {

        // Get island endpoints
        Location c1 = island.getTopLeftBound();
        Location c2 = island.getBottomRightBound();


        // Once all reset, recalculate light updates

    }

    private void fastUpdateBlock(World world, int x, int y, int z) {



    }

}
