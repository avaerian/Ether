package org.minerift.ether.island;

import org.minerift.ether.dimension.Dimension;
import org.minerift.ether.dimension.Dimensions;
import org.minerift.ether.math.Vec2i;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public record DeletedTile(Vec2i tile, Set<String> dimsToClear) {

    public static DeletedTile from(Vec2i tile, Dimensions dims) {
        Set<String> dimResrcs = new HashSet<>(dims.size());
        for(Dimension dim : dims) {
            dimResrcs.add(dim.getResourceLocation());
        }
        return new DeletedTile(tile, dimResrcs);
    }

    @Override
    public int hashCode() {
        return tile.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DeletedTile that)) return false;
        return Objects.equals(tile, that.tile) && Objects.equals(dimsToClear, that.dimsToClear);
    }
}
