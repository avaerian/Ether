package org.minerift.ether.listeners;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.minerift.ether.Ether;
import org.minerift.ether.dimension.Dimension;
import org.minerift.ether.island.IslandManager;

import static org.minerift.ether.util.BukkitUtils.asVec3i;

public class BlockBreakListener implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        final IslandManager islandMan = Ether.inst().getIslandManager();
        final Player plr = e.getPlayer();
        final Location loc = e.getBlock().getLocation();

        islandMan.getIslandAt(asVec3i(loc)).ifPresentOrElse((island) -> {

            if(island.isInAccessibleRegion(Dimension.from(loc.getWorld()), asVec3i(loc))) {
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
