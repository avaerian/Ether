package org.minerift.ether.database.sql.adapters;

import org.minerift.ether.util.pair.UUIDPair;

import java.util.UUID;

public class Pair2UuidsAdapter implements Adapter<UUIDPair, UUID[]> {
    @Override
    public UUID[] adaptTo(UUIDPair obj) {
        return obj.toArray();
    }

    @Override
    public UUIDPair adaptFrom(UUID[] obj) {
        return new UUIDPair(obj);
    }
}
