package org.minerift.ether.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.minerift.ether.Ether;
import org.minerift.ether.database.Result;
import org.minerift.ether.database.models.UserModel;
import org.minerift.ether.island.IslandRole;
import org.minerift.ether.user.EtherUser;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.minerift.ether.island.Island.INVALID_ID;

public class PlayerJoinQuitListener implements Listener {

    @EventHandler
    public void onPlayerLogin(AsyncPlayerPreLoginEvent event) {
        UUID uuid = event.getUniqueId();

        AtomicReference<EtherUser> user = new AtomicReference<>();
        Ether.inst().getDatabase().accessSync((access) -> {
            Result<EtherUser> res = access.selectById(UserModel.class, uuid);
            if(res.isEmpty()) {
                user.set(EtherUser.builder()
                        .setUUID(uuid)
                        .setIsland(INVALID_ID)
                        .setIslandRole(IslandRole.VISITOR)
                        .build());
                access.insert(UserModel.class, user.get());
            } else {
                user.set(res.getRecord(0).read());
            }
        }).join();

        Ether.inst().getUserManager().register(user.get());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {

        EtherUser user = Ether.inst().getUserManager().getUser(event.getPlayer().getUniqueId()).orElseThrow();
        Ether.inst().getDatabase().access((access) -> {
            Result<EtherUser> oldUser = access.selectById(UserModel.class, user.getUUID());
            if(oldUser.isEmpty()) {
                access.insert(UserModel.class, user);
            } else {
                access.update(UserModel.class, user);
            }

            Ether.inst().getUserManager().unregister(user);
        });

    }

}
