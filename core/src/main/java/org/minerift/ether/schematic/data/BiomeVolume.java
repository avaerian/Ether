package org.minerift.ether.schematic.data;

import org.minerift.ether.debug.NotWorking;
import org.minerift.ether.nms.world.Biome;
import org.minerift.ether.util.fn.ByteIterator;

public class BiomeVolume extends Volume<Biome<?>> {


    public static BiomeVolume.Builder builder() {
        return new BiomeVolume.Builder();
    }

    public static BiomeVolume.Lazy ofLazy() {
        return new BiomeVolume.Lazy(null, null, null, 0,0,0);
    }

    public BiomeVolume(Array3DOrder order, byte[] data, BytePalette<Biome<?>> palette, int width, int height, int length) {
        super(order, data, palette, width, height, length);
    }

    // TODO: review
    public boolean isEmpty() {
        if(palette.size() == 0) {
            return true;
        }
        byte res = 0;
        for(byte b : data) {
            res |= b;
        }
        return res == 0;
    }

    public static class Builder extends Volume.Builder<BiomeVolume, BiomeVolume.Builder, Biome<?>> {
        @Override
        public BiomeVolume build() {
            return new BiomeVolume(order, data, palette, width, height, length);
        }
    }

    @NotWorking
    @Deprecated // needs a lot of work
    public static class Lazy extends BiomeVolume {

        public static final byte[] EMPTY_DATA = new byte[0];

        @Override
        public boolean isEmpty() {
            return super.isEmpty();
        }

        @Override
        public ByteIterator byteIterator() {
            return super.byteIterator();
        }

        @Override
        public byte setData(byte b, int x, int y, int z) {
            return super.setData(b, x, y, z);
        }

        @Override
        public byte[] getData() {
            return super.getData();
        }

        @Override
        public Biome<?> getDataAt(int idx) {
            return super.getDataAt(idx);
        }

        public Lazy(Array3DOrder order, byte[] data, BytePalette<Biome<?>> palette, int width, int height, int length) {
            super(order, EMPTY_DATA, palette, width, height, length);
        }
    }
}
