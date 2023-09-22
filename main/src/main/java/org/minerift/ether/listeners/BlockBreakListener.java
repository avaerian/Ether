package org.minerift.ether.listeners;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.minerift.ether.Ether;
import org.minerift.ether.island.IslandManager;
import org.minerift.ether.math.Vec2i;

public class BlockBreakListener implements Listener {

    private IslandManager islandManager;

    public BlockBreakListener() {
        //this.islandManager = EtherPlugin.getInstance().getIslandManager();
        this.islandManager = Ether.getIslandManager();
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {

        final Player plr = e.getPlayer();
        final Location location = e.getBlock().getLocation();

        islandManager.getIslandAt(location).ifPresentOrElse((island) -> {

            final Vec2i tile = island.getTile();
            // TODO

        },
        // Else
        () -> plr.sendMessage("Island doesn't exist at this tile!"));

    }

}
