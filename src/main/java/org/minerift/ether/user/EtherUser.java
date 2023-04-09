package org.minerift.ether.user;

import org.minerift.ether.island.Island;
import org.minerift.ether.island.IslandPermission;
import org.minerift.ether.island.IslandRole;

import java.util.Optional;
import java.util.UUID;

public class EtherUser {

    private UUID uuid;

    private Island island;
    private IslandRole role;

    // TODO: implement (builder pattern?)
    public EtherUser() {

    }

    public Optional<Island> getIsland() {
        return Optional.ofNullable(island);
    }

    public void setIsland(Island island) {
        this.island = island;
    }

    public boolean hasPermission(Island island, IslandPermission permission) {
        IslandRole islandRole = island.isTeamMember(this) ? role : IslandRole.VISITOR;
        return island.getPermissions().hasPermission(islandRole, permission);
    }

}
