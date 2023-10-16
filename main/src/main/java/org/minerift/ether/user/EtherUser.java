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

    public static Builder builder() {
        return new Builder();
    }

    private EtherUser(Builder builder) {

        this.island = builder.island;
        this.role = builder.role;
        this.uuid = builder.uuid;

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

    public static class Builder {

        private Island island;
        private IslandRole role;
        private UUID uuid;

        public Builder() {
            this.role = IslandRole.VISITOR;
        }

        public EtherUser build() {
            return new EtherUser(this);
        }

        public Builder setUUID(UUID uuid) {
            this.uuid = uuid;
            return this;
        }

        public Builder setIsland(Island island) {
            this.island = island;
            return this;
        }

        public Builder setIslandRole(IslandRole role) {
            this.role = role;
            return this;
        }
    }

}
