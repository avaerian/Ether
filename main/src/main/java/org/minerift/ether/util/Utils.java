package org.minerift.ether.util;

import java.util.Arrays;
import java.util.function.Supplier;

public class Utils {

    public static int bool2Int(boolean b) {
        return (b ? 1 : 0);
    }

    public static Object[] joinArrays(Object[] first, Object[] second) {
        Object[] joined = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, joined, first.length, second.length);
        return joined;
    }

    public static <E extends Exception> void ensure(boolean predicate, Supplier<E> ex) throws E {
        if(!predicate) {
            throw ex.get();
        }
    }

}
