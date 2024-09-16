package org.minerift.ether.util;

import com.google.common.escape.Escaper;
import com.google.common.escape.Escapers;

import java.util.Arrays;
import java.util.function.Supplier;

public class Utils {

    public static int bool2Int(boolean b) {
        return b ? 1 : 0;
    }

    public static <T> T[] joinArrays(T[] first, T[] second) {
        T[] joined = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, joined, first.length, second.length);
        return joined;
    }

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
}
