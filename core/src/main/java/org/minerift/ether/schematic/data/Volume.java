package org.minerift.ether.schematic.data;

import org.jetbrains.annotations.NotNull;
import org.minerift.ether.math.Vec3i;
import org.minerift.ether.util.fn.IBuilder;
import org.minerift.ether.util.iterator.ByteIterator;
import org.minerift.ether.world.Archetype;

import java.util.Iterator;
import java.util.function.BiFunction;

import static java.lang.String.format;

// T -> element type
// A -> archetype
public abstract class Volume<T, A extends Archetype> implements Iterable<A> {

    @Deprecated
    public interface ToArchetype<T extends Archetype> extends BiFunction<Vec3i, String, T> {
        // empty
    }

    // TODO: implement
    @NotNull
    @Override
    public Iterator<A> iterator() {
        throw new UnsupportedOperationException("unimplemented");
    }

    public ByteIterator byteIterator() {
        return ByteIterator.of(data);
    }

    /***
     * REVISING SCHEMATIC API:
     * - blocks have a pallette and byte[] of all block pallette types ordered YZX
     * - blocks have a location lookup for block entities
     * - biomes have a pallette and byte[] of all biome pallette types ordered YZX
     * - entities have an array of each archetype (data and pos)
     *
     */

    protected final Array3DOrder order;
    protected final BytePalette<T> palette;
    protected final byte[] data;
    protected final int width, height, length;
    protected final ToArchetype<A> toArchetype;


    public Volume(Array3DOrder order, byte[] data, BytePalette<T> palette, int width, int height, int length, ToArchetype<A> toArchetype) {
        this.order = order;
        this.data = data;
        this.palette = palette;
        this.width = width;
        this.height = height;
        this.length = length;
        this.toArchetype = toArchetype;
    }

    public Volume(int width, int height, int length, ToArchetype<A> toArchetype) {
        this(Array3DOrder.YZX, new byte[width * height * length], new BytePalette<>(), width, height, length, toArchetype);
    }

    public byte setData(byte b, Vec3i loc) {
        return setData(b, loc.getX(), loc.getY(), loc.getZ());
    }

    // Returns original data, sets new data
    public byte setData(byte b, int x, int y, int z) {
        int idx = order.flatten(width, length, x, y, z);
        if(idx >= data.length) {
            throw new IllegalArgumentException(
                    format("Coords exceed the volume bounds (Dim: %d, %d, %d), (Req: %d, %d, %d)",
                            width, height, length, x, y, z));
        }

        if(!palette.containsKey(b)) {
            throw new IllegalArgumentException(b + " is not registered in the volume pallette");
        }

        byte old = data[idx];
        data[idx] = b;
        return old;
    }

    public byte[] getData() {
        return data;
    }

    public T getDataAt(int idx) {
        return palette.get(data[idx]);
    }

    public T getDataAt(int x, int y, int z) {
        return getDataAt(order.flatten(width, length, x, y, z));
    }

    public T getDataAt(Vec3i pos) {
        return getDataAt(pos.getX(), pos.getY(), pos.getZ());
    }

    public BytePalette<T> getPalette() {
        return palette;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getLength() {
        return length;
    }

    public Vec3i getDimensions() {
        return new Vec3i(getWidth(), getHeight(), getLength());
    }

    public static abstract class Builder<
            V extends Volume<T, A>,
            B extends Volume.Builder<V, B, T, A>,
            T,
            A extends Archetype> implements IBuilder<V> {
        protected Array3DOrder order;
        protected int width, height, length;
        protected byte[] data;
        protected BytePalette<T> palette;


        // defaults
        protected Builder() {
            this.width = 0;
            this.height = 0;
            this.length = 0;
            this.data = new byte[0];
            this.palette = null;
        }

        public B setOrder(Array3DOrder order) {
            this.order = order;
            return (B) this;
        }

        public B setData(byte[] data) {
            this.data = data;
            return (B) this;
        }

        public B setPalette(BytePalette<T> palette) {
            this.palette = palette;
            return (B) this;
        }

        public B addPaletteEntry(byte id, T data) {
            if(palette == null) {
                this.palette = new BytePalette<>();
            }
            palette.add(id, data, false);
            return (B) this;
        }

        public B setDimensions(int width, int height, int length) {
            this.width = width;
            this.height = height;
            this.length = length;
            return (B) this;
        }

        public B setDimensions(Vec3i dim) {
            return setDimensions(dim.getX(), dim.getY(), dim.getZ());
        }
    }
}
