package org.minerift.ether.util.pair;

import java.util.UUID;

public class UUIDPair extends SameTypePair<UUID> {
    public UUIDPair(UUID first, UUID second) {
        super(first, second);
    }

    public UUIDPair(UUID[] pair) {
        super(pair);
    }

    public UUID[] toArray() {
        return toArray(UUID[]::new);
    }
}
