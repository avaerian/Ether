package org.minerift.ether.schematic.data;

import org.minerift.ether.math.Vec3i;
import org.minerift.ether.util.fn.IBuilder;
import org.minerift.ether.util.fn.ByteIterator;

import static java.lang.String.format;

public abstract class Volume<T> {

    protected final Array3DOrder order;
    protected final BytePalette<T> palette;
    protected final byte[] data;
    protected final int width, height, length;

    public Volume(Array3DOrder order, byte[] data, BytePalette<T> palette, int width, int height, int length) {
        this.order = order;
        this.data = data;
        this.palette = palette;
        this.width = width;
        this.height = height;
        this.length = length;
    }

    public Volume(int width, int height, int length) {
        this(Array3DOrder.YZX, new byte[width * height * length], BytePalette.of(), width, height, length);
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
            throw new IllegalArgumentException(b + " is not registered in the volume palette");
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

    public ByteIterator byteIterator() {
        return ByteIterator.of(data);
    }

    public static abstract class Builder<
            V extends Volume<T>,
            B extends Volume.Builder<V, B, T>,
            T> implements IBuilder<V> {
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
            this.order = Array3DOrder.YZX;
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
                this.palette = BytePalette.of();
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

        // Getters

        public Array3DOrder getOrder() {
            return order;
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

        public byte[] getData() {
            return data;
        }

        public BytePalette<T> getPalette() {
            return palette;
        }
    }
}
