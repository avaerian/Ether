package org.minerift.ether.schematic.transform;

import org.jetbrains.annotations.NotNull;
import org.minerift.ether.math.Vec3i;
import org.minerift.ether.schematic.data.Array3DOrder;
import org.minerift.ether.util.fn.IBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

// List of transforms to apply
// TODO: review if necessary
public class Transforms implements Iterable<Transform> {

    private final List<Transform> transforms;

    public Transforms(List<Transform> transforms) {
        this.transforms = transforms;
    }

    public byte[] apply(Array3DOrder order, Vec3i dim, byte[] src) {
        return apply(order, dim.getX(), dim.getY(), dim.getZ(), src);
    }

    public byte[] apply(Array3DOrder order, int width, int height, int len, byte[] src) {
        byte[] res = src;
        for(Transform t : transforms) {
            res = t.transform(order, width, height, len, src);
        }
        return res;
    }

    public List<Transform> view() {
        return transforms;
    }

    public static Transforms.Builder builder() {
        return new Builder();
    }

    @Override
    public @NotNull Iterator<Transform> iterator() {
        return transforms.iterator();
    }

    public static class Builder implements IBuilder<Transforms> {

        private static final Supplier<List<Transform>> LIST_IMPL = ArrayList::new;

        private List<Transform> transforms;

        private Builder() {
            this.transforms = Collections.emptyList();
        }

        public Builder add(Transform transform) {
            if(transforms == Collections.EMPTY_LIST) {
                this.transforms = LIST_IMPL.get();
            }
            transforms.add(transform);
            return this;
        }

        @Override
        public Transforms build() {
            return new Transforms(Collections.unmodifiableList(transforms));
        }
    }

}
