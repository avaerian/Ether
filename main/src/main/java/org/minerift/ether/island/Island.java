package org.minerift.ether.island;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.minerift.ether.user.EtherUser;
import org.minerift.ether.util.math.Vec2i;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class Island {

    private long topLeftBound, bottomRightBound;

    // These 2 pieces of data can be calculated from each other
    private int id;
    private Vec2i tile;

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

    public Vec2i getTile() {
        return tile;
    }

    public Set<EtherUser> getTeamMembers() {
        return Set.copyOf(members);
    }

    public Set<EtherUser> getTeamMembersWithRole(IslandRole role) {
        return members.stream().filter(member -> member.getIslandRole() == role).collect(Collectors.toSet());
    }

    public EtherUser getOwner() {
        return getTeamMembersWithRole(IslandRole.OWNER).iterator().next();
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

    public long getTopLeftBoundRaw() {
        return topLeftBound;
    }

    public long getBottomRightBoundRaw() {
        return bottomRightBound;
    }

    public Chunk getTopLeftBound(World world) {
        return world.getChunkAt(topLeftBound);
    }

    public Chunk getBottomRightBound(World world) {
        return world.getChunkAt(bottomRightBound);
    }

    public static Island.Builder builder() {
        return new Island.Builder();
    }

    public static class Builder {

        private Vec2i tile;
        private int id;
        private long topLeftBound, bottomRightBound;
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
        public Builder setTile(Vec2i tile, boolean withId) {
            this.tile = tile;
            if(withId) this.id = tile.getTileId();
            return this;
        }

        public Builder setDeleted(boolean isDeleted) {
            this.isDeleted = isDeleted;
            return this;
        }

        public Builder definePermissions(IslandRole role, EnumSet<IslandPermission> rolePermissions) {
            this.permissions = new PermissionSet();
            permissions.set(role, rolePermissions);
            return this;
        }

        public Builder definePermissions(IslandRole role, IslandPermission ... rolePermissions) {
            this.permissions = new PermissionSet();
            permissions.set(role, rolePermissions);
            return this;
        }

        public Builder setPermission(IslandRole role, IslandPermission rolePermission) {
            this.permissions = new PermissionSet();
            permissions.set(role, rolePermission);
            return this;
        }

        public Builder addTeamMember(EtherUser user, IslandRole role) {
            members.add(user);
            user.setIslandRole(role);
            return this;
        }

        public Builder setTopLeftBound(int x, int z) {
            return setTopLeftBound(Chunk.getChunkKey(x, z));
        }

        public Builder setBottomRightBound(int x, int z) {
            return setBottomRightBound(Chunk.getChunkKey(x, z));
        }

        // Corner 1
        public Builder setTopLeftBound(long topLeftBound) {
            this.topLeftBound = topLeftBound;
            return this;
        }

        // Corner 2
        public Builder setBottomRightBound(long bottomRightBound) {
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
