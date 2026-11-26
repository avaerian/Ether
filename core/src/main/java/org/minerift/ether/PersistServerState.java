package org.minerift.ether;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.minerift.ether.debug.Debug;
import org.minerift.ether.math.Vec2i;
import org.minerift.ether.util.nbt.NbtSerializable;
import org.minerift.ether.util.nbt.tags.Tag;
import org.minerift.ether.util.nbt.tags.TagTypes;
import org.minerift.ether.util.nbt.tags.array.ByteArrayTag;
import org.minerift.ether.util.nbt.tags.container.CompoundTag;
import org.minerift.ether.util.nbt.tags.container.MismatchedTypeException;
import org.minerift.ether.util.nbt.tags.container.NoTagFoundException;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.*;

@AllArgsConstructor
public class PersistServerState implements NbtSerializable {

    private static final String DIM_ID_2_WORLD_UUID = "dimId2WorldUUID";

    public static PersistServerState from(CompoundTag root) throws NoTagFoundException, MismatchedTypeException {
        CompoundTag dimLookupTag = root.getCompound(DIM_ID_2_WORLD_UUID);
        Map<String, UUID> dimId2WorldUID = new HashMap<>(dimLookupTag.size());
        for(Map.Entry<String, Tag> e : dimLookupTag) {
            ByteArrayTag tag = e.getValue().as(TagTypes.BYTE_ARRAY);
            byte[] buf = tag.getValue();
            if(buf.length != 16)
                throw new IllegalArgumentException("Expected 16 bytes for world UUID, got " + buf.length);
            long ms = 0, ls = 0;
            for(int i = 0; i < 8; i++)
                ms |= (long) (buf[7 - i] & 0xFF) << (i * 8);
            for(int i = 0; i < 8; i++)
                ls |= (long) (buf[15 - i] & 0xFF) << (i * 8);
            UUID uuid = new UUID(ms, ls);
            dimId2WorldUID.put(e.getKey(), uuid);
        }

        return new PersistServerState(dimId2WorldUID, new HashSet<>()); // TODO
    }

    public Map<String, UUID> dimId2WorldUID;
    public final Collection<Vec2i> purgeQueue;

    public PersistServerState() {
        this.dimId2WorldUID = new HashMap<>();
        this.purgeQueue = new HashSet<>();
    }


    @Override
    public CompoundTag serializeNbt() {
        CompoundTag root = new CompoundTag();

        CompoundTag dimLookupTag = new CompoundTag(DIM_ID_2_WORLD_UUID);
        for(Map.Entry<String, UUID> e : dimId2WorldUID.entrySet()) {
            byte[] uuid = new byte[16];
            for(int i = 0; i < 8; i++)
                uuid[7 - i] = (byte) ((e.getValue().getMostSignificantBits() >>> (i * 8)) & 0xFF);
            for(int i = 0; i < 8; i++)
                uuid[15 - i] = (byte) ((e.getValue().getLeastSignificantBits() >>> (i * 8)) & 0xFF);
            dimLookupTag.addTag(new ByteArrayTag(e.getKey(), uuid));
        }
        root.addTag(dimLookupTag);

        return root;
    }

    @Debug
    public static void main(String[] args) {
        UUID test = UUID.randomUUID();
        byte[] uuid = new byte[16];
        for(int i = 0; i < 8; i++)
            uuid[7 - i] = (byte) ((test.getMostSignificantBits() >>> (i * 8)) & 0xFF);
        for(int i = 0; i < 8; i++)
            uuid[15 - i] = (byte) ((test.getLeastSignificantBits() >>> (i * 8)) & 0xFF);

        ByteBuffer buf = ByteBuffer.wrap(uuid);
        //long ms = buf.getLong();
        //long ls = buf.getLong();

        long ms = 0, ls = 0;
        for(int i = 0; i < 8; i++)
            ms |= (long) (uuid[7 - i] & 0xFF) << (i * 8);
        for(int i = 0; i < 8; i++)
            ls |= (long) (uuid[15 - i] & 0xFF) << (i * 8);
        UUID uuid0 = new UUID(ms, ls);
        System.out.println("uuid0 " + uuid0);

        ByteBuffer _buf = ByteBuffer.allocate(16);
        _buf.putLong(test.getMostSignificantBits());
        _buf.putLong(test.getLeastSignificantBits());
        System.out.println(Arrays.toString(_buf.array()));

        System.out.println("ms " + Long.toBinaryString(test.getMostSignificantBits()));
        System.out.println("ls " + Long.toBinaryString(test.getLeastSignificantBits()));
        System.out.println("ms " + Long.toBinaryString(ms));
        System.out.println("ls " + Long.toBinaryString(ls));
        UUID newUUID = new UUID(ms, ls);

        System.out.println(Arrays.toString(uuid));
        System.out.println(test);
        System.out.println(newUUID);
    }
}
