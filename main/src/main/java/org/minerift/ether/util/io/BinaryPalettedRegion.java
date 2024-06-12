package org.minerift.ether.util.io;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.bytes.ByteOpenHashSet;
import it.unimi.dsi.fastutil.bytes.ByteSet;
import it.unimi.dsi.fastutil.objects.Object2ByteMap;
import it.unimi.dsi.fastutil.objects.Object2ByteOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.minerift.ether.Secrets;
import org.minerift.ether.math.Vec3i;
import org.minerift.ether.schematic.readers.sponge.SchematicReaderContext;
import org.minerift.ether.schematic.readers.sponge.SpongeSchematicReader;
import org.minerift.ether.util.iterator.ByteBufferIterator;
import org.minerift.ether.util.nbt.tags.IntTag;
import org.minerift.ether.util.nbt.tags.Tag;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

import static org.minerift.ether.schematic.readers.sponge.SchematicNBTFields.NBT_BLOCK_DATA;
import static org.minerift.ether.schematic.readers.sponge.SchematicNBTFields.NBT_PALETTE;

public class BinaryPalettedRegion implements Iterable<Byte> {

    public static void main(String[] args) throws IOException {

        SchematicReaderContext ctx = SchematicReaderContext.from(new File(Secrets.LOCAL_SCHEM_FILE_LOC, "test_schem1.schem"));
        SpongeSchematicReader.ReadStages.INIT.read(ctx);

        Map<String, Tag> paletteRaw = ctx.rootView.getSectionView(NBT_PALETTE).orElseThrow().getSectionTags();
        byte[] rawData = ctx.rootView.getByteArray(NBT_BLOCK_DATA).orElseThrow();

        BinaryPalettedRegion.Builder regionBuilder = BinaryPalettedRegion.builder(rawData, ctx.builder.getDimensions());
        paletteRaw.forEach((res, id) -> regionBuilder.registerResourceKey(res, ((IntTag)id).getValue().byteValue()));
        BinaryPalettedRegion region = regionBuilder.build();

        //System.out.println(Arrays.toString(region.data.array()));
        System.out.println(region.idToKeyMap);
        ByteBuffer mapDump = BinaryFormat.dumpMap(region.idToKeyMap);
        mapDump.rewind();
        System.out.println("Id2KeyMap Dump: " + Arrays.toString(mapDump.array()));
        Byte2ObjectMap<String> rebuiltKeyMap = BinaryFormat.readMap(mapDump);
        System.out.println(rebuiltKeyMap);
        System.out.println(rebuiltKeyMap.size());
        System.out.println("Equal? " + rebuiltKeyMap.equals(region.idToKeyMap));

        for(int x = 0; x < region.width; x++) {
            for(int y = 0; y < region.height; y++) {
                for(int z = 0; z < region.length; z++) {
                    //getIndexAt(z, x, y, region.width, region.length);
                    System.out.println("new idx: " + ( z + (y * region.width) + (x * region.width * region.length) )); // ZYX, z varies the most
                }
            }
        }

        ctx.close();
    }

    public static BinaryPalettedRegion.Builder builder(Vec3i dim) {
        return builder(dim.getX(), dim.getY(), dim.getZ());
    }

    public static BinaryPalettedRegion.Builder builder(int width, int height, int length) {
        return new Builder(width, height, length);
    }

    public static BinaryPalettedRegion.Builder builder(byte[] data, Vec3i dim) {
        return builder(data, dim.getX(), dim.getY(), dim.getZ());
    }

    public static BinaryPalettedRegion.Builder builder(byte[] data, int width, int height, int length) {
        return new Builder(data, width, height, length);
    }

    private final int width, height, length;
    private final Byte2ObjectMap<String> idToKeyMap;
    private final ByteBuffer data;

    public BinaryPalettedRegion(Byte2ObjectMap<String> idToKeyMap, ByteBuffer data, int width, int height, int length) {
        this.idToKeyMap = idToKeyMap;
        this.data = data;
        this.width = width;
        this.height = height;
        this.length = length;
    }

    public BinaryPalettedRegion(Byte2ObjectMap<String> idToKeyMap, byte[] data, int width, int height, int length) {
        this(idToKeyMap, ByteBuffer.wrap(data), width, height, length);
    }

    public BinaryPalettedRegion(Byte2ObjectMap<String> idToKeyMap, byte[] data, Vec3i dim) {
        this(idToKeyMap, data, dim.getX(), dim.getY(), dim.getZ());
    }

    private BinaryPalettedRegion(BinaryPalettedRegion.Builder builder) {
        this(builder.getIdToKeyMap(), builder.buffer, builder.width, builder.height, builder.length);
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
        return new Vec3i(width, height, length);
    }

    public int getBufferCapacity() {
        return data.capacity();
    }

    public byte getDataAt(int idx) {
        return data.get(idx);
    }

    public byte getDataAt(int x, int y, int z) {
        return getDataAt(getIndexAt(x, y, z, width, length));
    }

    public String getNamespaceAt(byte id) {
        return idToKeyMap.get(id);
    }

    private static int getIndexAt(int x, int y, int z, int width, int length) {
        final int index = x + (z * width) + (y * width * length);
        System.out.println("idx: " + index);
        return index;
    }

    @NotNull
    @Override
    public Iterator<Byte> iterator() {
        return new ByteBufferIterator(data);
    }

    public static class Builder {

        public final int width, height, length;
        private Object2ByteMap<String> keyToIdMap;
        private final ByteSet registeredIds;
        public final ByteBuffer buffer;

        public Builder(int width, int height, int length) {
            this(ByteBuffer.allocate(width * height * length), width, height, length);
        }

        public Builder(byte[] data, int width, int height, int length) {
            this(ByteBuffer.wrap(data), width, height, length);
        }

        public Builder(ByteBuffer buffer, int width, int height, int length) {
            Preconditions.checkArgument(buffer.capacity() == width * height * length);
            this.width = width;
            this.height = height;
            this.length = length;
            this.keyToIdMap = new Object2ByteOpenHashMap<>(Byte.MAX_VALUE);
            this.registeredIds = new ByteOpenHashSet();
            this.buffer = buffer;
        }

        public BinaryPalettedRegion build() {
            return isValidPalette() ? new BinaryPalettedRegion(this) : null;
        }

        public Builder registerResourceKey(String key) {
            return registerResourceKey(key, (byte)keyToIdMap.size());
        }

        public Builder registerResourceKey(String key, int byteId) {
            return registerResourceKey(key, (byte) byteId);
        }

        public Builder registerResourceKey(String key, byte id) {
            if(keyToIdMap.size() >= Byte.MAX_VALUE) {
                throw new IllegalArgumentException("Cannot register anymore keys: registry is full!");
            }
            if(isRegisteredKey(key) || registeredIds.contains(id)) { // no overriding known ids
                throw new IllegalArgumentException("Cannot register existing key " + key + " with id " + id + "!");
            }

            keyToIdMap.put(key, id);
            registeredIds.add(id);
            return this;
        }

        public ByteSet getUsedIds() {
            ByteSet usedIds = new ByteOpenHashSet(registeredIds.size()); // best case scenario, these are the same size
            ByteBufferIterator.iterateOver(buffer).forEach(b -> usedIds.add(b.byteValue()));
            return usedIds;
        }

        public boolean isValidPalette() {
            if(registeredIds.size() != getUsedIds().size()) {
                return false;
            }

            return true;
        }

        public boolean isRegisteredKey(String key) {
            return keyToIdMap.containsKey(key);
        }

        public Builder writeNextEntry(String resourceKey) {
            if(!isRegisteredKey(resourceKey)) {
                registerResourceKey(resourceKey);
            }
            return writeNextByte(keyToIdMap.getByte(resourceKey));
        }

        public Builder writeEntry(String resourceKey, int idx) {
            if(!isRegisteredKey(resourceKey)) {
                registerResourceKey(resourceKey);
            }
            return writeByte(keyToIdMap.getByte(resourceKey), idx);
        }

        public Builder writeEntry(String resourceKey, int x, int y, int z) {
            return writeEntry(resourceKey, getIndexAt(x, y, z, width, length));
        }

        public Builder writeEntry(String resourceKey, Vec3i pos) {
            return writeEntry(resourceKey, pos.getX(), pos.getY(), pos.getZ());
        }

        public Builder writeNextByte(byte palettedId) {
            buffer.put(palettedId);
            return this;
        }

        public Builder writeByte(byte palettedId, int idx) {
            buffer.mark()
                    .put(idx, palettedId)
                    .reset();
            return this;
        }

        public Builder writeByte(byte palettedId, int x, int y, int z) {
            return writeByte(palettedId, getIndexAt(x, y, z, width, length));
        }

        public Builder writeByte(byte palettedId, Vec3i pos) {
            return writeByte(palettedId, pos.getX(), pos.getY(), pos.getZ());
        }

        public Object2ByteMap<String> getKeyToIdMap() {
            return keyToIdMap;
        }

        public Byte2ObjectMap<String> getIdToKeyMap() {
            Byte2ObjectMap<String> idToKeyMap = new Byte2ObjectOpenHashMap<>(keyToIdMap.size());
            keyToIdMap.forEach((key, id) -> idToKeyMap.put(id.byteValue(), key));
            return idToKeyMap;
        }

    }
}
