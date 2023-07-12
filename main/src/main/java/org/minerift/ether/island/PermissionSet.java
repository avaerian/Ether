package org.minerift.ether.island;

import java.util.*;
import java.util.stream.Collectors;

public class PermissionSet {

    private EnumMap<IslandRole, EnumSet<IslandPermission>> permissionSet;

    public PermissionSet() {
        this.permissionSet = new EnumMap<>(IslandRole.class);
    }

    public void setPermissions(IslandRole role, IslandPermission ... permissionsArr) {
        EnumSet<IslandPermission> permissions = EnumSet.noneOf(IslandPermission.class);
        Collections.addAll(permissions, permissionsArr);
        setPermissions(role, permissions);
    }

    public void setPermissions(IslandRole role, EnumSet<IslandPermission> permissions) {
        permissionSet.put(role, permissions);
    }

    public EnumSet<IslandPermission> getPermissions(IslandRole role) {
        return permissionSet.get(role);
    }

    public boolean hasPermission(IslandRole role, IslandPermission permission) {
        return permissionSet.get(role).contains(permission);
    }

    public boolean hasPermissions(IslandRole role, IslandPermission ... permissionsArr) {
        EnumSet<IslandPermission> permissions = EnumSet.noneOf(IslandPermission.class);
        Collections.addAll(permissions, permissionsArr);
        return hasPermissions(role, permissions);
    }

    public boolean hasPermissions(IslandRole role, EnumSet<IslandPermission> permissions) {
        return permissionSet.get(role).containsAll(permissions);
    }

    public void addPermission(IslandRole role, IslandPermission rolePermission) {
        permissionSet.get(role).add(rolePermission);
    }

    public void addPermissions(IslandRole role, EnumSet<IslandPermission> rolePermissions) {
        permissionSet.get(role).addAll(rolePermissions);
    }

    public void addPermissions(IslandRole role, IslandPermission ... rolePermissions) {
        EnumSet<IslandPermission> set = Arrays.stream(rolePermissions).collect(Collectors.toCollection(() -> EnumSet.noneOf(IslandPermission.class)));
        addPermissions(role, set);
    }

    public void removePermission(IslandRole role, IslandPermission rolePermission) {
        permissionSet.get(role).remove(rolePermission);
    }

    public void removePermissions(IslandRole role, EnumSet<IslandPermission> rolePermissions) {
        permissionSet.get(role).removeAll(rolePermissions);
    }

    public void removePermissions(IslandRole role, IslandPermission ... rolePermissions) {
        EnumSet<IslandPermission> set = Arrays.stream(rolePermissions).collect(Collectors.toCollection(() -> EnumSet.noneOf(IslandPermission.class)));
        removePermissions(role, set);
    }

}
