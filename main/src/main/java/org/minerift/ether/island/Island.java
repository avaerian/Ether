package org.minerift.ether.island;

import org.bukkit.Location;
import org.minerift.ether.user.EtherUser;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class Island {

    private Location topLeftBound, bottomRightBound;

    // These 2 pieces of data can be calculated from each other
    private int id;
    private Tile tile;

    // Team related fields
    private int maxTeamSize;

    // NOTE: hard-referencing users could consume memory over time
    // For lazy-loading, a proxy could solve this problem
    // This feature is not a problem for now
    private Set<EtherUser> members;

    private PermissionSet permissions;

    private boolean isDeleted;

    // Private constructor
    private Island(Island.Builder builder) {
        // TODO: load all values from builder to object
        this.tile = builder.tile;
        this.id = builder.id;
        this.isDeleted = builder.isDeleted;
        this.permissions = builder.permissions;
        this.members = builder.members;

        this.topLeftBound = builder.topLeftBound;
        this.bottomRightBound = builder.bottomRightBound;
    }


    public int getId() {
        return id;
    }

    public Tile getTile() {
        return tile;
    }

    public Set<EtherUser> getTeamMembers() {
        return Set.copyOf(members);
    }

    public Set<EtherUser> getTeamMembersWithRole(IslandRole role) {
        return members.stream().filter(member -> member.getIslandRole() == role).collect(Collectors.toSet());
    }

    public boolean isTeamMember(EtherUser user) {
        return members.contains(user);
    }

    public PermissionSet getPermissions() {
        return permissions;
    }

    public int getMaxTeamSize() {
        return maxTeamSize;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void markDeleted() {
        this.isDeleted = true;
    }

    public Location getTopLeftBound() {
        return topLeftBound;
    }

    public Location getBottomRightBound() {
        return bottomRightBound;
    }

    public static Island.Builder builder() {
        return new Island.Builder();
    }

    public static class Builder {

        private Tile tile;
        private int id;
        private Location topLeftBound, bottomRightBound;
        private boolean isDeleted = false;
        private PermissionSet permissions;

        // TODO: remove hard-reference for EtherUser
        private Set<EtherUser> members = new HashSet<>();

        /**
         *
         * @param tile
         * @param withId Whether the builder should also set the id from tile
         * @return
         */
        public Builder setTile(Tile tile, boolean withId) {
            this.tile = tile;
            if(withId) this.id = tile.getId();
            return this;
        }

        public Builder setDeleted(boolean isDeleted) {
            this.isDeleted = isDeleted;
            return this;
        }

        public Builder definePermissions(IslandRole role, EnumSet<IslandPermission> rolePermissions) {
            this.permissions = new PermissionSet();
            permissions.setPermissions(role, rolePermissions);
            return this;
        }

        public Builder definePermissions(IslandRole role, IslandPermission ... rolePermissions) {
            this.permissions = new PermissionSet();
            permissions.setPermissions(role, rolePermissions);
            return this;
        }

        public Builder setPermission(IslandRole role, IslandPermission rolePermission) {
            this.permissions = new PermissionSet();
            permissions.setPermissions(role, rolePermission);
            return this;
        }

        public Builder addTeamMember(EtherUser user, IslandRole role) {
            members.add(user);
            user.setIslandRole(role);
            return this;
        }

        // Corner 1
        public Builder setTopLeftBound(Location topLeftBound) {
            this.topLeftBound = topLeftBound;
            return this;
        }

        // Corner 2
        public Builder setBottomRightBound(Location bottomRightBound) {
            this.bottomRightBound = bottomRightBound;
            return this;
        }

        public Island build() {
            validate();
            return new Island(this);
        }

        // TODO: implement
        private void validate() {
            // Tile and Id are required
        }

    }

}
