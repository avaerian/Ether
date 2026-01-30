package org.minerift.ether.database.models;

import org.minerift.ether.math.Vec2i;

import java.util.Collection;
import java.util.HashSet;

// purely for interacting with the database
public class ServerData {

    public final Collection<Vec2i> purgeQueue;

    public ServerData(Collection<Vec2i> purgeQueue) {
        this.purgeQueue = purgeQueue;
    }

    public ServerData() {
        this.purgeQueue = new HashSet<>();
    }

}
