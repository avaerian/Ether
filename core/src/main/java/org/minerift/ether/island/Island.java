package org.minerift.ether.island;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.minerift.ether.Ether;
import org.minerift.ether.config.ConfigType;
import org.minerift.ether.config.main.MainConfig;
import org.minerift.ether.util.CanChange;
import org.minerift.ether.math.Maths;
import org.minerift.ether.math.Vec2i;
import org.minerift.ether.math.Vec3i;
import org.minerift.ether.user.EtherUser;
import org.minerift.ether.util.IBuilder;
import org.minerift.ether.world.ChunkCoords;

import java.util.*;
import java.util.stream.Collectors;

import static org.minerift.ether.util.BukkitUtils.asVec3i;

public class Island extends CanChange {

    public static final int INVALID_ID = -1;

    // TODO: when loading islands/players from database, load EtherUser's first (null island),
    //       then load Island's (set island for users and attach as island members here)
    //       This may require a DatabaseReaderContext or something similar for handling data loading

    private long blChunkZX, trChunkZX;
    private int accessibleRegionLength;

    // These 2 pieces of data can be calculated from each other
    private int id;
    private Vec2i tile;

    // Team related fields
    private int maxTeamSize;

    private Set<UUID> members;

    private PermissionSet permissions;

    private boolean isDeleted;

    // Private constructor
    private Island(Island.Builder builder) {

        // TODO: load all values from builder to object
        this.tile = builder.tile;
        this.id = builder.id;
        this.isDeleted = builder.isDeleted;
        this.permissions = builder.permissions;

        // TODO: figure out addTeamMember and handling/storing team members for islands
        this.members = builder.members.stream().map(EtherUser::getUUID).collect(Collectors.toSet());
        if(builder.owner != null) {
            members.add(builder.owner.getUUID());
        }
        //addTeamMember(builder.owner, IslandRole.OWNER);

        this.blChunkZX = builder.bottomLeftChunkBound;
        this.trChunkZX = builder.topRightChunkBound;

        setChanged(false);
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
        final int offset = (config.getTileLengthBlocks() / 2) - (config.getTileAccessibleAreaBlocks() / 2);

        Vec3i.Mutable blBlock = getBottomLeftBlock().asMutable().add(offset, 0, offset);
        Vec3i.Mutable trBlock = getTopRightBlock().asMutable().subtract(offset, 0, offset);

        return Maths.inRangeInclusive(blBlock, trBlock, loc);
    }

    public List<EtherUser> getTeamMembers() {
        List<EtherUser> users = new ArrayList<>(members.size());
        members.forEach(uuid -> users.add(Ether.getUserManager().getUser(uuid).orElse(null)));
        return users;
    }

    public List<EtherUser> getTeamMembersWithRole(IslandRole role) {
        return getTeamMembers().stream().filter(member -> member.getIslandRole() == role).toList();
    }

    public EtherUser getOwner() {
        return getTeamMembersWithRole(IslandRole.OWNER).iterator().next();
    }

    public boolean isTeamMember(EtherUser user) {
        return members.contains(user.getUUID());
    }

    public void addTeamMember(EtherUser user, IslandRole role) {
        members.add(user.getUUID());
        user.setIsland(this);
        user.setIslandRole(role);

        setChanged(true);
    }

    public void removeTeamMember(EtherUser user) {
        if(isTeamMember(user)) {
            members.remove(user.getUUID());
            user.setIsland((Integer) null);
            user.setIslandRole(IslandRole.VISITOR);

            setChanged(true);
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
        if(!isDeleted) {
            this.isDeleted = true;
            setChanged(true);
        }
    }

    public long getBottomLeftChunkKey() {
        return blChunkZX;
    }

    public long getTopRightChunkKey() {
        return trChunkZX;
    }

    public Vec2i getBottomLeftChunk() {
        return Maths.unpack(blChunkZX, Maths.PackingOrder.ZX);
    }

    public Vec2i getTopRightChunk() {
        return Maths.unpack(trChunkZX, Maths.PackingOrder.ZX);
    }

    // Mutable for math purposes
    // NOTE: Height is set to 0
    public Vec3i.Mutable getBottomLeftBlock() {
        final Vec2i blChunk = getBottomLeftChunk();
        return new Vec3i.Mutable(blChunk.getX() * 16, 0, blChunk.getZ() * 16);
    }

    // Mutable for math purposes
    // NOTE: Height is set to 0
    public Vec3i.Mutable getTopRightBlock() {
        final Vec2i.Mutable trChunk = getTopRightChunk().asMutable();
        trChunk.add(1, 1);
        return new Vec3i.Mutable((trChunk.getX() * 16) - 1, 0, (trChunk.getZ() * 16) - 1);
    }

    public Chunk getBottomLeftChunk(World world) {
        return world.getChunkAt(blChunkZX);
    }

    public Chunk getTopRightChunk(World world) {
        return world.getChunkAt(trChunkZX);
    }

    public static Island.Builder builder() {
        return new Island.Builder();
    }

    @Override
    public String toString() {
        return "Island{" +
                "id=" + id +
                ", tile=" + tile +
                ", isDeleted=" + isDeleted +
                ", bottomLeftBound=" + blChunkZX +
                ", topRightBound=" + trChunkZX +
                ", maxTeamSize=" + maxTeamSize +
                ", members=" + members +
                ", permissions=" + permissions +
                "}\n";
    }

    public static class Builder implements IBuilder<Island> {

        private Vec2i tile;
        private int id;
        private long bottomLeftChunkBound, topRightChunkBound;
        private boolean isDeleted;
        private PermissionSet permissions;

        private EtherUser owner;
        private List<EtherUser> members;

        private Builder() {
            this.members = new ArrayList<>();
        }

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

        public Builder setMembers(List<EtherUser> members) {
            this.members = members;
            return this;
        }

        public Builder setBottomLeftBound(int x, int z) {
            return setBottomLeftBound(ChunkCoords.getChunkKey(x, z));
        }

        public Builder setBottomLeftBound(Vec2i chunk) {
            return setBottomLeftBound(chunk.getX(), chunk.getZ());
        }

        public Builder setTopRightBound(int x, int z) {
            return setTopRightBound(ChunkCoords.getChunkKey(x, z));
        }

        public Builder setTopRightBound(Vec2i chunk) {
            return setTopRightBound(chunk.getX(), chunk.getZ());
        }

        // Corner 1
        public Builder setBottomLeftBound(long bottomLeftChunkBound) {
            this.bottomLeftChunkBound = bottomLeftChunkBound;
            return this;
        }

        // Corner 2
        public Builder setTopRightBound(long topRightChunkBound) {
            this.topRightChunkBound = topRightChunkBound;
            return this;
        }

        @Override
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
