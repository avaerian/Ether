package org.minerift.ether.user;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.minerift.ether.island.Island;
import org.minerift.ether.island.IslandPermission;
import org.minerift.ether.island.IslandRole;

import java.util.Optional;
import java.util.UUID;

public class EtherUser {

    private UUID uuid;

    private Island island;
    private IslandRole role;

    // TODO: implement builder pattern (good for loading persisted data cleanly)
    private EtherUser() {

        this.island = null;
        this.role = IslandRole.VISITOR;

    }

    public Optional<Island> getIsland() {
        return Optional.ofNullable(island);
    }

    public void setIsland(Island island) {
        this.island = island;
    }

    public IslandRole getIslandRole() {
        return role;
    }

    public void setIslandRole(IslandRole role) {
        this.role = role;
    }

    public UUID getUUID() {
        return uuid;
    }

    public OfflinePlayer getOfflinePlayer() {
        return Bukkit.getOfflinePlayer(uuid);
    }

    // Preferred method for retrieving a player
    // Returns the player if online, else an empty optional
    public Optional<Player> getPlayer() {
        return Optional.ofNullable(getOfflinePlayer().getPlayer());
    }

    public boolean hasPermission(Island island, IslandPermission permission) {
        IslandRole islandRole = island.isTeamMember(this) ? role : IslandRole.VISITOR;
        return island.getPermissions().has(islandRole, permission);
    }

}
