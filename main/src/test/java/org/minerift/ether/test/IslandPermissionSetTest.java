package org.minerift.ether.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.minerift.ether.island.*;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IslandPermissionSetTest {

    private Island island;
    private PermissionSet permissions;

    @BeforeEach
    public void setup() {
        this.island = Island.builder()
                .setTile(Tile.ZERO, true)
                .definePermissions(IslandRole.OWNER, EnumSet.allOf(IslandPermission.class))
                .definePermissions(IslandRole.MEMBER, EnumSet.range(IslandPermission.BLOCK_BREAK, IslandPermission.ENTITY_INTERACT))
                .definePermissions(IslandRole.VISITOR, EnumSet.noneOf(IslandPermission.class))
                .setDeleted(false)
                .build();

        this.permissions = island.getPermissions();
    }

    @Test
    public void registerIslandPermissionsTest() {

        permissions.setPermissions(IslandRole.OWNER, EnumSet.allOf(IslandPermission.class));
        permissions.setPermissions(IslandRole.MEMBER, EnumSet.range(IslandPermission.BLOCK_BREAK, IslandPermission.ENTITY_DAMAGE));
        permissions.removePermission(IslandRole.MEMBER, IslandPermission.ENTITY_DAMAGE);
        permissions.setPermissions(IslandRole.VISITOR, EnumSet.noneOf(IslandPermission.class));

        assertTrue(permissions.hasPermissions(IslandRole.OWNER, EnumSet.allOf(IslandPermission.class)));
        assertFalse(permissions.hasPermissions(IslandRole.MEMBER, EnumSet.allOf(IslandPermission.class)));
        assertFalse(permissions.hasPermission(IslandRole.VISITOR, IslandPermission.BLOCK_INTERACT));
    }

    // TODO: later
    /*@Test
    public void userIslandPermissionTest() {
        // TODO: should change to builder pattern
        EtherUser user = new EtherUser();

    }*/

}
