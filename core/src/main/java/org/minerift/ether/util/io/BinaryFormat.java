package org.minerift.ether.util.io;

import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectOpenHashMap;
import org.minerift.ether.util.pair.Pair;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Deprecated
public class BinaryFormat {

    public static class Section {
        private String name;
        private ByteBuffer data;

        public Section(String name, ByteBuffer data) {
            this.name = name;
            this.data = data;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public ByteBuffer getData() {
            return data;
        }
    }

    // Section: 4 parts (short textLength, char array (str of textLength), int bytedatalength, byte array of bytedatalength)

    public static Section readSection(ByteBuffer buffer) {
        short nameBytes = buffer.getShort();
        byte[] nameBuf = new byte[nameBytes];
        buffer.get(nameBuf);
        String name = new String(nameBuf, StandardCharsets.UTF_8);
        int dataBytes = buffer.getInt();
        ByteBuffer data = buffer.slice(buffer.position(), dataBytes);

        return new Section(name, data);
    }

    public static Section[] readSections(ByteBuffer buffer) {
        buffer.rewind();
        List<Section> sections = new ArrayList<>();
        while(buffer.hasRemaining()) {
            sections.add(readSection(buffer));
        }
        return sections.toArray(Section[]::new);
    }

    public static Map<String, Section> partitionSections(ByteBuffer buffer) {
        buffer.rewind();
        Map<String, Section> sections = new HashMap<>();
        while(buffer.hasRemaining()) {
            Section section = readSection(buffer);
            sections.put(section.getName(), section);
        }
        return sections;
    }

    // parts: (int mapSize) (byte id, int strByteLen, byte array of string)
    public static ByteBuffer dumpMap(Byte2ObjectMap<String> map) {
        int entryCount = map.size();
        int strSumByteSize = map.values().stream().mapToInt((str) -> str.getBytes().length).sum();
        int byteSize = Integer.BYTES + (entryCount * (Byte.BYTES + Integer.BYTES)) + strSumByteSize;

        ByteBuffer buffer = ByteBuffer.allocate(byteSize);
        buffer.putInt(entryCount); // int mapSize
        for(var entry : map.byte2ObjectEntrySet()) {
            buffer.put(entry.getByteKey()); // byte id
            byte[] strBytes = entry.getValue().getBytes(StandardCharsets.UTF_8);
            buffer.putInt(strBytes.length); // strByteLen
            buffer.put(strBytes); // byte array of string
        }

        return buffer;
    }

    public static Byte2ObjectMap<String> readMap(ByteBuffer buffer) {
        Pair<Byte, String>[] entries = readMapEntries(buffer);
        Byte2ObjectMap<String> map = new Byte2ObjectOpenHashMap<>(entries.length);
        for(Pair<Byte, String> entry : entries) {
            map.put(entry.getFirst().byteValue(), entry.getSecond());
        }
        return map;
    }

    public static Pair<Byte, String>[] readMapEntries(ByteBuffer buffer) {
        int entryCount = buffer.getInt();
        Pair<Byte, String>[] entries = new Pair[entryCount];
        for(int i = 0; i < entryCount; i++) {
            byte id = buffer.get(); // read id
            byte[] strBytes = new byte[buffer.getInt()]; // read str byte len
            buffer.get(strBytes); // read str bytes
            String str = new String(strBytes, StandardCharsets.UTF_8);

            entries[i] = new Pair<>(id, str);
        }
        return entries;
    }

}
