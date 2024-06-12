package org.minerift.ether.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.minerift.ether.Ether;
import org.minerift.ether.island.IslandRole;
import org.minerift.ether.user.EtherUser;

import java.util.UUID;

public class PlayerJoinQuitListener implements Listener {

    @EventHandler
    public void onPlayerLogin(AsyncPlayerPreLoginEvent event) {
        UUID uuid = event.getUniqueId();
        // TODO: attempt to fetch data from database, otherwise create new profile

        // Check if user is already registered
        if(Ether.getUserManager().getUser(uuid).isPresent()) {
            return;
        }

        EtherUser user = EtherUser.builder()
                .setUUID(uuid)
                .setIsland(null)
                .setIslandRole(IslandRole.VISITOR)
                .build();

        Ether.getUserManager().register(user);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {

    }

}
