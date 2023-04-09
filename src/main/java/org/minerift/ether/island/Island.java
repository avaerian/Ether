package org.minerift.ether.island;

import org.bukkit.Location;
import org.minerift.ether.GridAlgorithm;
import org.minerift.ether.user.EtherUser;

import java.util.HashSet;
import java.util.Set;

public class Island {

    private Location topLeftBound;
    private Location bottomRightBound;

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


    public static Island.Builder builder() {
        return new Island.Builder();
    }

    public static class Builder {

        private Tile tile;
        private int id;
        private boolean isDeleted = false;

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
