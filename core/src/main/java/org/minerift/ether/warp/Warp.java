package org.minerift.ether.warp;

import lombok.AllArgsConstructor;
import org.minerift.ether.dimension.Dimension;
import org.minerift.ether.world.Location;

@AllArgsConstructor
public class Warp {
    private final Location loc;
    private final Dimension dim;

    public Location getLocation() {
        return loc;
    }

    public Dimension getDimension() {
        return dim;
    }
}
