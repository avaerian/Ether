package org.minerift.ether.island;

import org.minerift.ether.debug.NeedsTesting;

import java.util.*;
import java.util.stream.Collectors;

// TODO: review this shit
@NeedsTesting
public class PermissionSet {

    // TODO: switch from Set to BitSet to test if IslandPermission ordinal is present?
    private EnumMap<IslandRole, EnumSet<IslandPermission>> permsMap;

    public PermissionSet() {
        this.permsMap = new EnumMap<>(IslandRole.class);
    }

    public PermissionSet set(IslandRole role, IslandPermission perm) {
        permsMap.put(role, EnumSet.of(perm));
        return this;
    }

    public void set(IslandRole role, IslandPermission ... perms) {
        EnumSet<IslandPermission> _perms = EnumSet.noneOf(IslandPermission.class);
        Collections.addAll(_perms, perms);
        set(role, _perms);
    }

    public PermissionSet set(IslandRole role, EnumSet<IslandPermission> perms) {
        permsMap.put(role, perms);
        return this;
    }

    public EnumSet<IslandPermission> get(IslandRole role) {
        return permsMap.get(role);
    }

    public boolean has(IslandRole role, IslandPermission permission) {
        return permsMap.get(role).contains(permission);
    }

    public boolean has(IslandRole role, IslandPermission ... perms) {
        EnumSet<IslandPermission> permissions = EnumSet.noneOf(IslandPermission.class);
        Collections.addAll(permissions, perms);
        return has(role, permissions);
    }

    public boolean has(IslandRole role, EnumSet<IslandPermission> perms) {
        return permsMap.get(role).containsAll(perms);
    }

    public PermissionSet add(IslandRole role, IslandPermission perm) {
        permsMap.get(role).add(perm);
        return this;
    }

    public PermissionSet add(IslandRole role, EnumSet<IslandPermission> perms) {
        this.permsMap.get(role).addAll(perms);
        return this;
    }

    public PermissionSet add(IslandRole role, IslandPermission ... perms) {
        EnumSet<IslandPermission> set = Arrays.stream(perms).collect(Collectors.toCollection(() -> EnumSet.noneOf(IslandPermission.class)));
        add(role, set);
        return this;
    }

    public PermissionSet remove(IslandRole role, IslandPermission perm) {
        permsMap.get(role).remove(perm);
        return this;
    }

    public PermissionSet remove(IslandRole role, EnumSet<IslandPermission> perms) {
        permsMap.get(role).removeAll(perms);
        return this;
    }

    public PermissionSet remove(IslandRole role, IslandPermission ... perms) {
        EnumSet<IslandPermission> set = Arrays.stream(perms).collect(Collectors.toCollection(() -> EnumSet.noneOf(IslandPermission.class)));
        remove(role, set);
        return this;
    }

}
