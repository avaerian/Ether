package org.minerift.ether.schematic.data;

import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ByteMap;
import it.unimi.dsi.fastutil.objects.Object2ByteOpenHashMap;
import org.minerift.ether.util.UnreachableException;

import static java.lang.String.format;

public class BytePalette<V> /* TODO: implements Map<Byte, V> */ {

    private final Byte2ObjectMap<V> palette;

    public BytePalette() {
        this.palette = new Byte2ObjectOpenHashMap<>();
    }

    public BytePalette(int expectedSize) {
        this.palette = new Byte2ObjectOpenHashMap<>(expectedSize);
    }

    public V add(byte b, V val) {
        return add(b, val, false);
    }

    public V add(byte b, V val, boolean replace) { // returns old value
        V existing = palette.get(b);
        if(existing != null) {
            if(!replace) {
                throw new IllegalArgumentException(
                        format("%b (%s) is already registered as %s and cannot be replaced", b, val, existing));
            }
            // TODO: logger
        }
        palette.put(b, val);
        return existing;
    }

    public V get(byte b) {
        V val = palette.get(b);
        if(val == null) {
            throw new IllegalStateException(b + " doesn't exist in byte palette");
        }
        return val;
    }

    public boolean containsKey(byte b) {
        return palette.containsKey(b);
    }

    public boolean containsValue(V val) {
        return palette.containsValue(val);
    }

    // TODO
    public V remove(byte b) {
        throw new UnreachableException("unimplemented");
    }

}
