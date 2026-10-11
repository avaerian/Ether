package org.minerift.ether.island;

import org.minerift.ether.Ether;
import org.minerift.ether.debug.NeedsTesting;

import java.util.*;
import java.util.logging.Level;

@NeedsTesting
public class PermissionSet {

    static {
        // should review this impl so this never occurs (could switch from longs
        // to BitSets if ever needed); at this moment this doesn't occur with the
        // small number of island permissions
        if(IslandPermission.VALUES.length > 64) {
            Ether.inst().getLogger().warn("Island permissions count exceeds 64");
        }
    }

    private long[] perms; // longs as the permission sets, indexed by the island roles

    public PermissionSet(long[] perms) {
        assert perms.length == IslandRole.values().length;
        this.perms = perms;
    }

    public PermissionSet() {
        this.perms = new long[IslandRole.values().length];
    }

    public long[] getPerms() {
        return Arrays.copyOf(perms, perms.length);
    }

    public long[] getPermsMut() {
        return perms;
    }

    public PermissionSet set(IslandRole role, IslandPermission perm) {
        perms[role.ordinal()] = 1 << perm.ordinal();
        return this;
    }

    public void set(IslandRole role, IslandPermission... set) {
        long roleSet = 0;
        for(IslandPermission perm : set) {
            roleSet |= 1 << perm.ordinal();
        }
        perms[role.ordinal()] = roleSet;
    }

    public PermissionSet set(IslandRole role, EnumSet<IslandPermission> set) {
        long roleSet = perms[role.ordinal()];
        for(IslandPermission perm : set) {
            roleSet |= 1 << perm.ordinal();
        }
        perms[role.ordinal()] = roleSet;
        return this;
    }

    public EnumSet<IslandPermission> get(IslandRole role) {
        EnumSet<IslandPermission> set = EnumSet.noneOf(IslandPermission.class);
        long bits = perms[role.ordinal()];
        int i;
        while((i = Long.numberOfLeadingZeros(bits)) != 64) {
            set.add(IslandPermission.VALUES[i]);
            bits &= ~(1L << i);
        }
        return set;
    }

    public boolean has(IslandRole role, IslandPermission perm) {
        return ( perms[role.ordinal()] & (1 << perm.ordinal()) ) != 0;
    }

    public boolean has(IslandRole role, IslandPermission... has) {
        for(IslandPermission perm : has) {
            if(!has(role, perm)) {
                return false;
            }
        }
        return true;
    }

    public boolean has(IslandRole role, EnumSet<IslandPermission> has) {
        for(IslandPermission perm : has) {
            if(!has(role, perm)) {
                return false;
            }
        }
        return true;
    }

    public PermissionSet add(IslandRole role, IslandPermission perm) {
        perms[role.ordinal()] |= 1 << perm.ordinal();
        return this;
    }

    public PermissionSet add(IslandRole role, EnumSet<IslandPermission> add) {
        long roleSet = 0;
        for(IslandPermission perm : add) {
            roleSet |= 1 << perm.ordinal();
        }
        perms[role.ordinal()] = roleSet;
        return this;
    }

    public PermissionSet add(IslandRole role, IslandPermission... add) {
        long roleSet = perms[role.ordinal()];
        for(IslandPermission perm : add) {
            roleSet |= 1 << perm.ordinal();
        }
        perms[role.ordinal()] = roleSet;
        return this;
    }

    public PermissionSet remove(IslandRole role, IslandPermission perm) {
        //long roleSet = perms[role.ordinal()];
        //roleSet &= ~(1 << perm.ordinal());
        perms[role.ordinal()] &= ~(1 << perm.ordinal());
        return this;
    }

    public PermissionSet remove(IslandRole role, IslandPermission... remove) {
        long roleSet = perms[role.ordinal()];
        for(IslandPermission perm : remove) {
            roleSet &= ~(1 << perm.ordinal());
        }
        perms[role.ordinal()] = roleSet;
        return this;
    }

    public PermissionSet remove(IslandRole role, EnumSet<IslandPermission> remove) {
        long roleSet = perms[role.ordinal()];
        for(IslandPermission perm : remove) {
            roleSet &= ~(1 << perm.ordinal());
        }
        perms[role.ordinal()] = roleSet;
        return this;
    }

}
