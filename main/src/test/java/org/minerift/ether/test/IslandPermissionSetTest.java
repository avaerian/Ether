package org.minerift.ether.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.minerift.ether.island.*;
import org.minerift.ether.math.Vec2i;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IslandPermissionSetTest {

    private Island island;
    private PermissionSet permissions;

    @BeforeEach
    public void setup() {
        this.island = Island.builder()
                .setTile(Vec2i.ZERO, true)
                .definePermissions(IslandRole.OWNER, EnumSet.allOf(IslandPermission.class))
                .definePermissions(IslandRole.MEMBER, EnumSet.range(IslandPermission.BLOCK_BREAK, IslandPermission.ENTITY_INTERACT))
                .definePermissions(IslandRole.VISITOR, EnumSet.noneOf(IslandPermission.class))
                .setDeleted(false)
                .build();

        this.permissions = island.getPermissions();
    }

    @Test
    public void registerIslandPermissionsTest() {

        permissions.set(IslandRole.OWNER, EnumSet.allOf(IslandPermission.class));
        permissions.set(IslandRole.MEMBER, EnumSet.range(IslandPermission.BLOCK_BREAK, IslandPermission.ENTITY_DAMAGE));
        permissions.remove(IslandRole.MEMBER, IslandPermission.ENTITY_DAMAGE);
        permissions.set(IslandRole.VISITOR, EnumSet.noneOf(IslandPermission.class));

        assertTrue(permissions.has(IslandRole.OWNER, EnumSet.allOf(IslandPermission.class)));
        assertFalse(permissions.has(IslandRole.MEMBER, EnumSet.allOf(IslandPermission.class)));
        assertFalse(permissions.has(IslandRole.VISITOR, IslandPermission.BLOCK_INTERACT));
    }

    // TODO: later
    /*@Test
    public void userIslandPermissionTest() {
        // TODO: should change to builder pattern
        EtherUser user = new EtherUser();

    }*/

}
