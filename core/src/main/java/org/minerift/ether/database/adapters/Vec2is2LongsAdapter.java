package org.minerift.ether.database.adapters;

import org.minerift.ether.math.Maths;
import org.minerift.ether.math.Vec2i;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Vec2is2LongsAdapter implements Adapter<Collection<Vec2i>, long[]> {

    @Override
    public long[] adaptTo(Collection<Vec2i> obj) {
        long[] ls = new long[obj.size()];
        int i = 0;
        for(Vec2i vec : obj) {
            ls[i++] = Maths.pack(vec, Maths.PackingOrder.ZX);
        }
        return new long[0];
    }

    @Override
    public Collection<Vec2i> adaptFrom(long[] obj) {
        List<Vec2i> vecs = new ArrayList<>(obj.length);
        for(long l : obj) {
            vecs.add(Maths.unpack(l, Maths.PackingOrder.ZX));
        }
        return vecs;
    }
}
