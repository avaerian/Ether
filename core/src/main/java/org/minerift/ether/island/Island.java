package org.minerift.ether.island;

import lombok.Getter;
import lombok.Setter;
import org.minerift.ether.Ether;
import org.minerift.ether.config.ConfigType;
import org.minerift.ether.config.main.MainConfig;
import org.minerift.ether.dimension.Dimension;
import org.minerift.ether.util.CanChange;
import org.minerift.ether.math.Maths;
import org.minerift.ether.math.Vec2i;
import org.minerift.ether.math.Vec3i;
import org.minerift.ether.user.EtherUser;
import org.minerift.ether.util.fn.IBuilder;
import org.minerift.ether.util.nbt.NbtSerializable;
import org.minerift.ether.util.nbt.NbtWriter;
import org.minerift.ether.util.nbt.tags.NbtOptions;
import org.minerift.ether.util.nbt.tags.container.CompoundTag;
import org.minerift.ether.warp.Warp;
import org.minerift.ether.world.Location;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.String.format;

public class Island extends CanChange implements NbtSerializable {

    public static final int INVALID_ID = -1;

    // TODO: when loading islands/players from database, load EtherUser's first (null island),
    //       then load Island's (set island for users and attach as island members here)
    //       This may require a DatabaseReaderContext or something similar for handling data loading

    @Getter private int id;
    @Getter private Vec2i tile;

    @Getter private Location spawn;

    @Deprecated
    @Getter private int maxTeamSize;

    private IslandValue.Mutable value;
    private UUID owner;
    private Set<UUID> members;
    private PermissionSet perms;
    private Set<String> dimsUnlocked; // dimension ids, NOT resource locations

    // per-dimension data: warp signs
    private List<Warp> warps;

    public static Island from(Vec2i tile, int id,
                              EtherUser owner, Set<EtherUser> members,
                              PermissionSet perms, IslandValue.Mutable value,
                              Set<String> dimsUnlocked) {
        return new Island(tile, id, owner.getUUID(),
                members.stream().map(EtherUser::getUUID).collect(Collectors.toSet()),
                perms, value, dimsUnlocked);
    }

    public static Island from(Vec2i tile, int id,
                              UUID owner, Set<UUID> members,
                              PermissionSet perms, IslandValue.Mutable value,
                              Set<String> dimsUnlocked) {
        return new Island(tile, id, owner, members, perms, value, dimsUnlocked);
    }

    public Island(Vec2i tile, int id,
                  UUID owner, Set<UUID> members,
                  PermissionSet perms, IslandValue.Mutable value,
                  Set<String> dimsUnlocked) {

        this.tile = tile;
        this.id = id;

        this.owner = owner;
        this.members = members;
        addTeamMember(owner, IslandRole.OWNER);
        this.perms = perms;

        this.value = value;
        this.dimsUnlocked = dimsUnlocked;

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


    public IslandValue getValue() {
        return value;
    }

    public boolean isInAccessibleRegion(Dimension dim, Vec3i loc) {
        final int offset = (dim.getTileLenBlocks() / 2) - (dim.getTileAccessibleLenBlocks() / 2);

        Vec3i.Mutable blBlock = getBottomLeftBlock(dim).asMutable().add(offset, 0, offset);
        Vec3i.Mutable trBlock = getTopRightBlock(dim).asMutable().subtract(offset, 0, offset);

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

    public Set<String> getUnlockedDimensionResources() {
        return dimsUnlocked;
    }

    public Set<Dimension> getUnlockedDimensions() {
        // skip any invalid/unregistered dimensions
        final MainConfig cfg = Ether.inst().getConfig(ConfigType.MAIN);
        Set<Dimension> dims = new HashSet<>();
        for(String sdim : dimsUnlocked) {
            Dimension dim = cfg.getDimensions().getByName(sdim);
            if(dim != null) {
                dims.add(dim);
            }
        }
        return dims;
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
            user.setIsland(INVALID_ID);
            user.setIslandRole(IslandRole.VISITOR);

            setChanged(true);
        }
    }

    public PermissionSet getPermissions() {
        return perms;
    }

    public Vec2i getBottomLeftChunk(Dimension dim) {
        return new Vec2i(
                tile.getX() * dim.getTileLenChunks(),
                tile.getZ() * dim.getTileLenChunks());
    }

    public Vec2i getTopRightChunk(Dimension dim) {
        return new Vec2i(
                (tile.getX() + 1) * dim.getTileLenChunks() - 1,
                (tile.getZ() + 1) * dim.getTileLenChunks() - 1);
    }

    // Mutable for math purposes
    // NOTE: Height is set to 0
    public Vec3i.Mutable getBottomLeftBlock(Dimension dim) {
        final Vec2i blChunk = getBottomLeftChunk(dim);
        return new Vec3i.Mutable(blChunk.getX() * 16, 0, blChunk.getZ() * 16);
    }

    // Mutable for math purposes
    // NOTE: Height is set to 0
    public Vec3i.Mutable getTopRightBlock(Dimension dim) {
        final Vec2i.Mutable trChunk = getTopRightChunk(dim).asMutable();
        trChunk.add(1, 1);
        return new Vec3i.Mutable((trChunk.getX() * 16) - 1, 0, (trChunk.getZ() * 16) - 1);
    }

    public static Island.Builder builder() {
        return new Island.Builder();
    }

    @Override
    public String toString() {
        return "Island{" +
                "id=" + id +
                ", tile=" + tile +
                ", maxTeamSize=" + maxTeamSize +
                ", members=" + members +
                ", permissions=" + perms +
                "}\n";
    }

    @Override
    public CompoundTag serializeNbt() {
        CompoundTag root = new CompoundTag();
        //root.addTag();
        return root;
    }

    @Override
    public int hashCode() {
        // TODO: better hash code
        NbtWriter writer = NbtWriter.withOptions()
                .options(NbtOptions.USE_NUNBT_IO)
                .build();
        writer.writeTag(serializeNbt());
        return writer.buf.hashCode();
    }

    @Getter
    public static class Builder implements IBuilder<Island> {

        private Vec2i tile;
        private int id;
        private Location spawn;

        private UUID owner;
        private Set<EtherUser> members;
        private PermissionSet perms;
        private IslandValue.Mutable value;
        private Set<String> dimsUnlocked;

        private Builder() {
            this.members = new HashSet<>();
            this.dimsUnlocked = new HashSet<>();
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

        public Builder setWorth(IslandValue.Mutable value) {
            this.value = value;
            return this;
        }

        public Builder setUnlockedDimensionResources(Set<String> dims) {
            this.dimsUnlocked = dims;
            return this;
        }

        public Builder setUnlockedDimensions(Set<Dimension> dims) {
            this.dimsUnlocked = dims.stream()
                    .map(Dimension::getResourceLocation).collect(Collectors.toSet());
            return this;
        }

        // tileBounds are formatted as blChunk, trChunk
        @Override
        public Island build() {
            return new Island(tile, id, owner,
                    members.stream().map(EtherUser::getUUID).collect(Collectors.toSet()),
                    perms, value, dimsUnlocked);
        }
    }

}
