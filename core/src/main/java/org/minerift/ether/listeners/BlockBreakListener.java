package org.minerift.ether.listeners;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.minerift.ether.Ether;
import org.minerift.ether.dimension.Dimension;
import org.minerift.ether.island.IslandManager;

public class BlockBreakListener implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {

        final IslandManager islandManager = Ether.inst().getIslandManager();
        final Player plr = e.getPlayer();
        final Location loc = e.getBlock().getLocation();

        islandManager.getIslandAt(loc).ifPresentOrElse((island) -> {

            // TODO
            if(island.isInAccessibleRegion(Dimension.from(loc.getWorld()), loc)) {
                plr.sendMessage("Broke block in accessible region!");
            } else {
                plr.sendMessage("Not in accessible region!");
            }
            plr.sendMessage("Island id: " + island.getId() + " / " + island.getTile());

        },
        // Else
        () -> plr.sendMessage("Island doesn't exist at this tile!"));

    }

}
