package org.minerift.ether.schematic.transform;

import org.jetbrains.annotations.NotNull;
import org.minerift.ether.math.Vec3d;
import org.minerift.ether.math.Vec3i;
import org.minerift.ether.schematic.data.Array3DOrder;
import org.minerift.ether.util.fn.IBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

// List of transforms to apply
public class Transforms implements Iterable<Transform> {

    public static Transforms.Builder builder() {
        return new Builder();
    }

    public static Transforms of(Transform t) {
        return new Transforms(List.of(t));
    }

    public static Transforms of(Transform ... ts) {
        return new Transforms(List.of(ts));
    }

    private final List<Transform> transforms;

    public Transforms(List<Transform> transforms) {
        this.transforms = transforms;
    }

    public Transform.Result<byte[]> apply(Array3DOrder order, Vec3i dim, byte[] src) {
        Transform.Result<byte[]> res = new Transform.Result<>(dim, src);
        for(Transform t : transforms) {
            res = t.transform(order, res.dim, res.out);
        }
        return res;
    }

    public Transform.Result<byte[]> apply(Array3DOrder order, int width, int height, int len, byte[] src) {
        return apply(order, new Vec3i(width, height, len), src);
    }

    public Transform.Result<Vec3i> apply(Vec3i dim, Vec3i vec) {
        Transform.Result<Vec3i> res = new Transform.Result<>(dim, vec);
        for(Transform t : transforms) {
            res = t.transformVec(res.dim, res.out);
        }
        return res;
    }

    public Transform.Result<Vec3i> apply(int w, int h, int l, Vec3i vec) {
        return apply(new Vec3i(w, h, l), vec);
    }

    public Transform.Result<Vec3d> apply(Vec3i dim, Vec3d vec) {
        Transform.Result<Vec3d> res = new Transform.Result<>(dim, vec);
        for(Transform t : transforms) {
            res = t.transformVec(res.dim, res.out);
        }
        return res;
    }

    /*public Vec3i.Mutable applyMut(Vec3i dim, Vec3i.Mutable vec) {}*/

    public List<Transform> view() {
        return transforms;
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
