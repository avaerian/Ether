package org.minerift.ether.island;

import org.minerift.ether.math.Vec2i;
import org.minerift.ether.util.UnreachableException;
import org.minerift.ether.world.ChunkCoords;

// manager for regions in the provided world
// TODO: move to world package ??
public class Regions {

    // Test if region is empty.
    // "Empty" is defined as all air blocks except the generated world (no player modifications)
    public boolean isEmpty(Vec2i tile) {
        Vec2i blChunk; // TODO
        Vec2i trChunk; // TODO

        //ChunkCoords.rangeClosed()
        throw new UnreachableException("unimplemented");
    }
}
