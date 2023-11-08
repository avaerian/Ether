package org.minerift.ether.island;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.minerift.ether.Ether;
import org.minerift.ether.config.ConfigType;
import org.minerift.ether.config.main.MainConfig;
import org.minerift.ether.math.Maths;
import org.minerift.ether.math.Vec2i;
import org.minerift.ether.math.Vec3i;
import org.minerift.ether.user.EtherUser;
import org.minerift.ether.util.BukkitUtils;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.minerift.ether.util.BukkitUtils.asVec3i;

public class Island {

    // TODO: when loading islands/players from database, load EtherUser's first (null island),
    //       then load Island's (set island for users and attach as island members here)
    //       This may require a DatabaseReaderContext or something similar for handling data loading

    private long bottomLeftBound, topRightBound;

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

        this.members = new HashSet<>();
        addTeamMember(builder.owner, IslandRole.OWNER);

        this.bottomLeftBound = builder.bottomLeftBound;
        this.topRightBound = builder.topRightBound;
    }


    public int getId() {
        return id;
    }

    public Vec2i getTile() {
        return tile;
    }

    public boolean isInAccessibleRegion(Location loc) {
        return isInAccessibleRegion(asVec3i(loc));
    }

    public boolean isInAccessibleRegion(Vec3i loc) {
        final MainConfig config = Ether.getConfig(ConfigType.MAIN);
        final int offset = (config.getTileSize() / 2) - (config.getTileAccessibleArea() / 2);

        Vec3i.Mutable blBlock = getBottomLeftBlock().asMutable().add(offset, 0, offset);
        Vec3i.Mutable trBlock = getTopRightBlock().asMutable().subtract(offset, 0, offset);

        return Maths.inRangeInclusive(blBlock, trBlock, loc);
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

    public void addTeamMember(EtherUser user, IslandRole role) {
        members.add(user);
        user.setIsland(this);
        user.setIslandRole(role);
    }

    public void removeTeamMember(EtherUser user) {
        if(isTeamMember(user)) {
            members.remove(user);
            user.setIsland(null);
            user.setIslandRole(IslandRole.VISITOR);
        }
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

    public long getBottomLeftChunkKey() {
        return bottomLeftBound;
    }

    public long getTopRightChunkKey() {
        return topRightBound;
    }

    public Vec2i getBottomLeftTile() {
        return new Vec2i((int) bottomLeftBound, (int) (bottomLeftBound >> 32));
    }

    public Vec2i getTopRightTile() {
        return new Vec2i((int) topRightBound, (int) (topRightBound >> 32));
    }

    public Vec3i getBottomLeftBlock() {
        // TODO
        return BukkitUtils.getVec3iAt(getBottomLeftTile()).asMutable().setY(0);
    }

    public Vec3i getTopRightBlock() {
        final Vec2i.Mutable trBound = getTopRightTile().asMutable();
        trBound.add(1, 1);
        final Vec3i.Mutable trBlock = BukkitUtils.getVec3iAt(trBound).asMutable();
        trBlock.subtract(1, 0, 1);
        return trBlock;
    }

    public Chunk getBottomLeftChunk(World world) {
        // TODO: extract(?) world.getMinHeight()
        return world.getChunkAt(bottomLeftBound);
    }

    public Chunk getTopRightChunk(World world) {
        return world.getChunkAt(topRightBound);
    }

    public static Island.Builder builder() {
        return new Island.Builder();
    }

    public static class Builder {

        private Vec2i tile;
        private int id;
        private long bottomLeftBound, topRightBound;
        private boolean isDeleted;
        private PermissionSet permissions;

        private EtherUser owner;
        private Set<EtherUser> members;

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

        // TODO: either call setOwner or setMembers (setOwner for creating new island, setMembers for database/persist loading)
        public Builder setOwner(EtherUser owner) {
            this.owner = owner;
            return this;
        }

        public Builder setMembers(Set<EtherUser> members) {
            this.members = members;
            return this;
        }

        public Builder setBottomLeftBound(int x, int z) {
            return setBottomLeftBound(Chunk.getChunkKey(x, z));
        }

        public Builder setTopRightBound(int x, int z) {
            return setTopRightBound(Chunk.getChunkKey(x, z));
        }

        // Corner 1
        public Builder setBottomLeftBound(long bottomLeftBound) {
            this.bottomLeftBound = bottomLeftBound;
            return this;
        }

        // Corner 2
        public Builder setTopRightBound(long topRightBound) {
            this.topRightBound = topRightBound;
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
