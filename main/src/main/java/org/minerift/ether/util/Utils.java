package org.minerift.ether.util;

import java.util.Arrays;

public class Utils {

    public static Object[] joinArrays(Object[] first, Object[] second) {
        Object[] joined = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, joined, first.length, second.length);
        return joined;
    }

}
