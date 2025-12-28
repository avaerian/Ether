package org.minerift.ether.util;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.escape.Escaper;
import com.google.common.escape.Escapers;
import it.unimi.dsi.fastutil.ints.IntList;
import org.minerift.ether.Ether;
import org.minerift.ether.debug.NeedsTesting;
import org.minerift.ether.util.fn.IntBiConsumer;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Supplier;

public class Utils {

    @Deprecated public static final BiMap<?, ?> EMPTY_BIMAP = ImmutableBiMap.of();

    private Utils() {
        throw new IllegalStateException();
    }

    @Deprecated
    public static <K, V> BiMap<K, V> emptyBiMap() {
        return (BiMap<K, V>) EMPTY_BIMAP;
    }

    // TODO: review
    @NeedsTesting
    public static boolean isPow2(int i) {
        return (i == Integer.highestOneBit(i));
    }

    // TODO: proper Javadocs
    // return index, or negated if not found
    public static int binarySearch(IntList list, int key) {
        int low = 0;
        int high = list.size()-1;

        while (low <= high) {
            int midIdx = (low + high) >>> 1;
            int midVal = list.getInt(midIdx);
            int cmp = midVal - key; // compare fn at its simplest

            if (cmp < 0)
                low = midIdx + 1;
            else if (cmp > 0)
                high = midIdx - 1;
            else
                return midIdx; // key found
        }
        return ~low;  // key not found
    }

    /**
     * Takes the String name and attempts to return the enum Type.
     * If the name is invalid, fail silently and return null.
     * @param str raw string of the enum
     * @return translated string into enum
     */
    public static <E extends Enum<E>> E valueOfSilent(Class<E> enumClazz, String str) {
        try {
            return Enum.valueOf(enumClazz, str);
        } catch (IllegalArgumentException ignore) {
            return null;
        }
    }

    public static int boolToInt(boolean b) {
        return b ? 1 : 0;
    }

    public static <T> T[] joinArrays(T[] first, T[] second) {
        T[] joined = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, joined, first.length, second.length);
        return joined;
    }

    public static <T> boolean contains(T[] array, T obj) {
        for(T element : array) {
            if(element.equals(obj)) {
                return true;
            }
        }
        return false;
    }

    public static <K, V> void enumerateMap(Map<K, V> map, IntBiConsumer<Map.Entry<K, V>> consumer) {
        int i = 0;
        for(Iterator<Map.Entry<K, V>> it = map.entrySet().iterator(); it.hasNext(); i++) {
            consumer.accept(i, it.next());
        }
    }

    // TODO: review; move to SQLUtils?
    @Deprecated
    public static String fixString(String str) {
        String _str = str;
        if(str.charAt(0) == '"' && str.charAt(str.length() - 1) == '"') {
            _str = str.substring(1, str.length() - 1);
        }

        Escaper backslashEscaper = Escapers.builder()
                .addEscape('\\', "")
                .build();
        _str = backslashEscaper.escape(_str);
        return _str;
    }

    public static <E extends Exception> void ensure(boolean predicate, Supplier<E> ex) throws E {
        if(!predicate) {
            throw ex.get();
        }
    }

    public static boolean isJUnitTest(Class<?> clazz) {
        return isJUnitTest(clazz.getPackage());
    }

    public static boolean isJUnitTest(Package pckage) {
        return pckage.getName().endsWith("test");
    }
}
