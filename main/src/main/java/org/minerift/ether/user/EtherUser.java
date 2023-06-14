package org.minerift.ether.user;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.minerift.ether.island.Island;
import org.minerift.ether.island.IslandPermission;
import org.minerift.ether.island.IslandRole;

import java.util.Optional;
import java.util.UUID;

public class EtherUser {

    private UUID uuid;

    private Island island;
    private IslandRole role;

    // TODO: implement builder pattern
    public EtherUser() {

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

    public boolean hasPermission(Island island, IslandPermission permission) {
        IslandRole islandRole = island.isTeamMember(this) ? role : IslandRole.VISITOR;
        return island.getPermissions().hasPermission(islandRole, permission);
    }

}
