package org.minerift.ether.island;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;

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

}
