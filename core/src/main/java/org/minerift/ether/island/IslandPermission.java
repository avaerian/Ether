package org.minerift.ether.island;

import org.minerift.ether.debug.NeedsReview;

public enum IslandPermission {

    BLOCK_BREAK,
    BLOCK_PLACE,
    BLOCK_INTERACT,
    ENTITY_INTERACT,
    ENTITY_DAMAGE,

    ;

    @NeedsReview
    public static final IslandPermission[] VALUES = IslandPermission.values();
}
