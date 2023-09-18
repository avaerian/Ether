package org.minerift.ether.island;

import java.util.*;
import java.util.stream.Collectors;

public class PermissionSet {

    private EnumMap<IslandRole, EnumSet<IslandPermission>> permissionSet;

    public PermissionSet() {
        this.permissionSet = new EnumMap<>(IslandRole.class);
    }

    public void set(IslandRole role, IslandPermission ... permissionsArr) {
        EnumSet<IslandPermission> permissions = EnumSet.noneOf(IslandPermission.class);
        Collections.addAll(permissions, permissionsArr);
        set(role, permissions);
    }

    public void set(IslandRole role, EnumSet<IslandPermission> permissions) {
        permissionSet.put(role, permissions);
    }

    public EnumSet<IslandPermission> get(IslandRole role) {
        return permissionSet.get(role);
    }

    public boolean has(IslandRole role, IslandPermission permission) {
        return permissionSet.get(role).contains(permission);
    }

    public boolean has(IslandRole role, IslandPermission ... permissionsArr) {
        EnumSet<IslandPermission> permissions = EnumSet.noneOf(IslandPermission.class);
        Collections.addAll(permissions, permissionsArr);
        return has(role, permissions);
    }

    public boolean has(IslandRole role, EnumSet<IslandPermission> permissions) {
        return permissionSet.get(role).containsAll(permissions);
    }

    public void add(IslandRole role, IslandPermission rolePermission) {
        permissionSet.get(role).add(rolePermission);
    }

    public void add(IslandRole role, EnumSet<IslandPermission> rolePermissions) {
        permissionSet.get(role).addAll(rolePermissions);
    }

    public void add(IslandRole role, IslandPermission ... rolePermissions) {
        EnumSet<IslandPermission> set = Arrays.stream(rolePermissions).collect(Collectors.toCollection(() -> EnumSet.noneOf(IslandPermission.class)));
        add(role, set);
    }

    public void remove(IslandRole role, IslandPermission rolePermission) {
        permissionSet.get(role).remove(rolePermission);
    }

    public void remove(IslandRole role, EnumSet<IslandPermission> rolePermissions) {
        permissionSet.get(role).removeAll(rolePermissions);
    }

    public void remove(IslandRole role, IslandPermission ... rolePermissions) {
        EnumSet<IslandPermission> set = Arrays.stream(rolePermissions).collect(Collectors.toCollection(() -> EnumSet.noneOf(IslandPermission.class)));
        remove(role, set);
    }

}
