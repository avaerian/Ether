package org.minerift.ether.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.minerift.ether.EtherPlugin;
import org.minerift.ether.island.Island;
import org.minerift.ether.island.IslandManager;
import org.minerift.ether.island.Tile;

import java.util.Optional;

public class BlockBreakListener implements Listener {

    private IslandManager islandManager;

    public BlockBreakListener() {
        this.islandManager = EtherPlugin.getInstance().getIslandManager();
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {

        Player plr = e.getPlayer();

        Optional<Island> possibleIsland = islandManager.getIslandAt(e.getBlock().getLocation());
        if(possibleIsland.isEmpty()) {
            plr.sendMessage("Island doesn't exist at this tile!");
            return;
        }

        Island island = possibleIsland.get();
        Tile tile = island.getTile();




    }

}
