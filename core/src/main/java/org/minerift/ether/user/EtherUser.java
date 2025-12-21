package org.minerift.ether.user;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.minerift.ether.Ether;
import org.minerift.ether.util.CanChange;
import org.minerift.ether.island.Island;
import org.minerift.ether.island.IslandPermission;
import org.minerift.ether.island.IslandRole;
import org.minerift.ether.util.fn.IBuilder;

import java.util.Optional;
import java.util.UUID;

public class EtherUser extends CanChange {

    private final UUID uuid;

    private Integer islandId;
    private IslandRole role;

    public static Builder builder() {
        return new Builder();
    }

    private EtherUser(Builder builder) {
        this.islandId = builder.islandId;
        this.role = builder.role;
        this.uuid = builder.uuid;
        setChanged(false);
    }

    public Optional<Island> getIsland() {
        return Ether.inst().getIslandManager().getIslandAt(islandId);
    }

    public void setIsland(Island island) {
        setIsland(island.getId());
    }

    public void setIsland(Integer islandId) {
        if(this.islandId == null || !this.islandId.equals(islandId)) {
            this.islandId = islandId;
            setChanged(true);
        }
    }

    public IslandRole getIslandRole() {
        return role;
    }

    public void setIslandRole(IslandRole role) {
        this.role = role;
        setChanged(true);
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

    @Override
    public String toString() {
        return "EtherUser{" +
                "uuid=" + uuid +
                ", islandId=" + islandId +
                ", role=" + role +
                '}';
    }

    public static class Builder implements IBuilder<EtherUser> {

        private Integer islandId;
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

        public Builder setIsland(Integer islandId) {
            this.islandId = islandId;
            return this;
        }

        public Builder setIslandRole(IslandRole role) {
            this.role = role;
            return this;
        }
    }
}
