package org.minerift.ether.island;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.minerift.ether.Ether;
import org.minerift.ether.config.ConfigType;
import org.minerift.ether.config.main.MainConfig;
import org.minerift.ether.dimension.Dimension;
import org.minerift.ether.math.Vec2l;
import org.minerift.ether.util.CanChange;
import org.minerift.ether.math.Maths;
import org.minerift.ether.math.Vec2i;
import org.minerift.ether.math.Vec3i;
import org.minerift.ether.user.EtherUser;
import org.minerift.ether.util.fn.IBuilder;
import org.minerift.ether.world.ChunkCoords;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.String.format;
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

    private IslandWorth.Mutable value;
    private UUID owner;
    private Set<UUID> members;
    private PermissionSet perms;

    public static Island from(Vec2i tile, int id, Vec2l chunkBounds,
                              EtherUser owner, Set<EtherUser> members,
                              PermissionSet perms, IslandWorth.Mutable worth) {
        return new Island(tile, id, chunkBounds.getXl(), chunkBounds.getZl(), owner.getUUID(),
                members.stream().map(EtherUser::getUUID).collect(Collectors.toSet()),
                perms, worth);
    }

    public static Island from(Vec2i tile, int id, Vec2l chunkBounds,
                              UUID owner, Set<UUID> members,
                              PermissionSet perms, IslandWorth.Mutable worth) {
        return new Island(tile, id, chunkBounds.getXl(), chunkBounds.getZl(),
                owner, members, perms, worth);
    }

    public Island(Vec2i tile, int id,
                  long blChunkZX, long trChunkZX,
                  UUID owner, Set<UUID> members,
                  PermissionSet perms, IslandWorth.Mutable worth) {

        this.tile = tile;
        this.id = id;
        this.perms = perms;

        this.owner = owner;
        this.members = members;
        addTeamMember(owner, IslandRole.OWNER);
        this.value = worth;

        this.blChunkZX = blChunkZX;
        this.trChunkZX = trChunkZX;

        setChanged(false);
    }

    public static Vec2i getBottomLeftBound(Dimension dim, Vec2i tile) {
        return new Vec2i(
                (tile.getX() * dim.getTileLenChunks()) - 1,
                (tile.getZ() * dim.getTileLenChunks()) - 1);
    }

    public static Vec2i getTopRightBound(Dimension dim, Vec2i tile) {
        return new Vec2i(
                (dim.getTileLenChunks() * (tile.getX() + 1)) - 1,
                (dim.getTileLenChunks() * (tile.getZ() + 1)) - 1);
    }


    public int getId() {
        return id;
    }

    public Vec2i getTile() {
        return tile;
    }

    public IslandWorth getWorth() {
        return value;
    }

    public boolean isInAccessibleRegion(Dimension dim, Location loc) {
        return isInAccessibleRegion(dim, asVec3i(loc));
    }

    public boolean isInAccessibleRegion(Dimension dim, Vec3i loc) {
        final MainConfig config = Ether.inst().getConfig(ConfigType.MAIN);
        final int offset = (dim.getTileLenBlocks() / 2) - (dim.getTileAccessibleLenBlocks() / 2);

        Vec3i.Mutable blBlock = getBottomLeftBlock().asMutable().add(offset, 0, offset);
        Vec3i.Mutable trBlock = getTopRightBlock().asMutable().subtract(offset, 0, offset);

        return Maths.inRangeInclusiveI(blBlock, trBlock, loc);
    }

    public List<EtherUser> getTeamMembers() {
        List<EtherUser> users = new ArrayList<>(members.size());
        members.forEach(uuid -> users.add(Ether.inst().getUserManager().getUser(uuid).orElse(null)));
        return users;
    }

    public List<EtherUser> getTeamMembersWithRole(IslandRole role) {
        return getTeamMembers().stream().filter(member -> member.getIslandRole() == role).toList();
    }

    public EtherUser getOwner() {
        return Ether.inst().getUserManager().getUser(owner).orElseThrow(() -> new IllegalStateException("Owner " + owner + " is offline"));
        //return getTeamMembersWithRole(IslandRole.OWNER).iterator().next();
    }

    public boolean isTeamMember(EtherUser user) {
        return members.contains(user.getUUID());
    }

    public void addTeamMember(UUID uuid, IslandRole role) {
        final EtherUser user = Ether.inst().getUserManager()
                .getUser(uuid).orElseThrow(() -> new IllegalStateException(uuid + " is offline"));

        // this should never be an issue; we would have a UserRegistry/UserManager mismatch
        user.getPlayer().orElseThrow(() -> new IllegalStateException(
                format("Attempted to add offline user %s to island %d, %s",
                        user.getOfflinePlayer().getName(), id, tile)));

        members.add(user.getUUID());
        user.setIsland(this);
        user.setIslandRole(role);

        setChanged(true);
    }

    public void addTeamMember(EtherUser user, IslandRole role) {
        addTeamMember(user.getUUID(), role);
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
        return perms;
    }

    public int getMaxTeamSize() {
        return maxTeamSize;
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
                ", bottomLeftBound=" + blChunkZX +
                ", topRightBound=" + trChunkZX +
                ", maxTeamSize=" + maxTeamSize +
                ", members=" + members +
                ", permissions=" + perms +
                "}\n";
    }

    public static class Builder implements IBuilder<Island> {

        private Vec2i tile;
        private int id;
        private long blChunk, trChunk;

        private UUID owner;
        private Set<EtherUser> members;
        private PermissionSet perms;
        private IslandWorth.Mutable value;

        private Builder() {
            this.members = new HashSet<>();
        }

        /**
         * Set the tile, optionally with the additional id from the tile coords.
         *
         * @param tile tile coordinates to place the island; NOTE: should align with the id
         * @param withId whether the builder should also set the id from tile
         *
         * @return this builder reflecting the changes made
         */
        // Javadocs need to be written properly
        public Builder setTile(Vec2i tile, boolean withId) {
            this.tile = tile;
            if(withId) {
                this.id = tile.getTileId();
            }
            return this;
        }

        public Builder definePermissions(IslandRole role, EnumSet<IslandPermission> rolePermissions) {
            this.perms = new PermissionSet();
            perms.set(role, rolePermissions);
            return this;
        }

        public Builder definePermissions(IslandRole role, IslandPermission... rolePermissions) {
            this.perms = new PermissionSet();
            perms.set(role, rolePermissions);
            return this;
        }

        public Builder setPermission(IslandRole role, IslandPermission rolePermission) {
            this.perms = new PermissionSet();
            perms.set(role, rolePermission);
            return this;
        }

        public Builder setPermissionSet(PermissionSet perms) {
            this.perms = perms;
            return this;
        }

        public Builder setOwner(EtherUser owner) {
            this.owner = owner.getUUID();
            return this;
        }

        public Builder setOwner(UUID owner) {
            this.owner = owner;
            return this;
        }

        public Builder setMembers(Set<EtherUser> members) {
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

        public Builder setBottomLeftBound(long blChunk) {
            this.blChunk = blChunk;
            return this;
        }

        public Builder setTopRightBound(long trChunk) {
            this.trChunk = trChunk;
            return this;
        }

        public Builder setWorth(IslandWorth.Mutable value) {
            this.value = value;
            return this;
        }

        // tileBounds are formatted as blChunk, trChunk
        @Override
        public Island build() {
            return new Island(tile, id, blChunk, trChunk, owner,
                    members.stream().map(EtherUser::getUUID).collect(Collectors.toSet()),
                    perms, value);
        }
    }

}
