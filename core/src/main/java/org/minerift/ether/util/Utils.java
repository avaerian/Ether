package org.minerift.ether.util;

import com.google.common.escape.Escaper;
import com.google.common.escape.Escapers;
import com.google.errorprone.annotations.DoNotCall;
import org.minerift.ether.database.PrimitiveType;
import org.minerift.ether.debug.Debug;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Supplier;

public class Utils {

    private Utils() {
        throw new IllegalStateException();
    }

    public static int boolToInt(boolean b) {
        return b ? 1 : 0;
    }

    public static int bytesToInt(byte[] bytes) {
        return bytesToInt(bytes, 0);
    }

    public static int bytesToInt(byte[] bytes, int offset) {
        final int byteCount = Integer.BYTES;
        if(bytes.length < byteCount) throw new IllegalArgumentException("Need an array of " + byteCount + " bytes");
        int byteOffset = (offset * byteCount);
        return  ((bytes[0 + byteOffset] & 0xFF) << 24) |
                ((bytes[1 + byteOffset] & 0xFF) << 16) |
                ((bytes[2 + byteOffset] & 0xFF) << 8)  |
                ((bytes[3 + byteOffset] & 0xFF));
    }

    public static long bytesToLong(byte[] bytes) {
        return bytesToLong(bytes, 0);
    }

    public static long bytesToLong(byte[] bytes, int offset) {
        final int byteCount = Long.BYTES;
        if(bytes.length < byteCount) throw new IllegalArgumentException("Need an array of " + byteCount + " bytes");
        int byteOffset = (offset * byteCount);
        return  ((bytes[0 + byteOffset] & 0xFF) << 56) |
                ((bytes[1 + byteOffset] & 0xFF) << 48) |
                ((bytes[2 + byteOffset] & 0xFF) << 40) |
                ((bytes[3 + byteOffset] & 0xFF) << 32) |
                ((bytes[4 + byteOffset] & 0xFF) << 24) |
                ((bytes[5 + byteOffset] & 0xFF) << 16) |
                ((bytes[6 + byteOffset] & 0xFF) << 8)  |
                ((bytes[7 + byteOffset] & 0xFF));
    }

    public static float bytesToFloat(byte[] bytes) {
        return bytesToFloat(bytes, 0);
    }

    public static float bytesToFloat(byte[] bytes, int offset) {
        return Float.intBitsToFloat(bytesToInt(bytes, offset));
    }

    public static double bytesToDouble(byte[] bytes) {
        return bytesToDouble(bytes, 0);
    }

    public static double bytesToDouble(byte[] bytes, int offset) {
        return Double.longBitsToDouble(bytesToLong(bytes, offset));
    }

    public static byte[] shortsToBytes(short[] array) {
        final int primBytes = Short.BYTES;
        byte[] bytes = new byte[array.length * primBytes];
        for(int i = 0; i < array.length; i++) {
            for(int j = 0; j < primBytes; j++) {
                bytes[j + (i * primBytes)] = (byte) ((array[i] >> ((primBytes - 1 - j) * 8)) & 0xFF);
            }
        }
        return bytes;
    }

    public static byte[] intsToBytes(int[] array) {
        final int primBytes = Integer.BYTES;
        byte[] bytes = new byte[array.length * primBytes];
        for(int i = 0; i < array.length; i++) {
            for(int j = 0; j < primBytes; j++) {
                bytes[j + (i * primBytes)] = (byte) ((array[i] >> ((primBytes - 1 - j) * 8)) & 0xFF);
            }
        }
        return bytes;
    }

    public static byte[] longsToBytes(long[] array) {
        final int primBytes = Long.BYTES;
        byte[] bytes = new byte[array.length * primBytes];
        for(int i = 0; i < array.length; i++) {
            for(int j = 0; j < primBytes; j++) {
                bytes[j + (i * primBytes)] = (byte) ((array[i] >> ((primBytes - 1 - j) * 8)) & 0xFF);
            }
        }
        return bytes;
    }

    public static byte[] floatsToBytes(float[] array) {
        final int primBytes = Float.BYTES;
        byte[] bytes = new byte[array.length * primBytes];
        for(int i = 0; i < array.length; i++) {
            for(int j = 0; j < primBytes; j++) {
                bytes[j + (i * primBytes)] = (byte) ((Float.floatToRawIntBits((array[i])) >> ((primBytes - 1 - j) * 8)) & 0xFF);
            }
        }
        return bytes;
    }

    public static byte[] doublesToBytes(double[] array) {
        final int primBytes = Double.BYTES;
        byte[] bytes = new byte[array.length * primBytes];
        for(int i = 0; i < array.length; i++) {
            for(int j = 0; j < primBytes; j++) {
                bytes[j + (i * primBytes)] = (byte) ((Double.doubleToRawLongBits(array[i]) >> ((primBytes - 1 - j) * 8)) & 0xFF);
            }
        }
        return bytes;
    }

    @Debug
    public static void main(String[] args) {
        int[] ints = new int[] { 5, 12, 69, 420 };
        byte[] bytes = intsToBytes(ints);
        System.out.println(Arrays.toString(ints));
        System.out.println(Arrays.toString(bytes));
        for(int i = 0; i < bytes.length / Integer.BYTES; i++) {
            System.out.println(bytesToInt(bytes, i));
        }

        String[] strings = new String[] {"Hello", "world!"};

        System.out.println("ints len: " + getArrayLength(ints));
        System.out.println("bytes len: " + getArrayLength(bytes));
        System.out.println("strs len: " + getArrayLength(strings));
    }

    public static int getArrayLength(Object array) {
        return switch (array) {
            case byte[] bytes -> bytes.length;
            case short[] shorts -> shorts.length;
            case int[] ints -> ints.length;
            case long[] longs -> longs.length;
            case float[] floats -> floats.length;
            case double[] doubles -> doubles.length;
            case Object[] objs -> objs.length;
            default -> throw new IllegalArgumentException(array.getClass() + " is not an array!");
        };
    }

    public static Object getArrayElement(Object array, int index) {
        return switch (array) {
            case byte[] bytes -> bytes[index];
            case short[] shorts -> shorts[index];
            case int[] ints -> ints[index];
            case long[] longs -> longs[index];
            case float[] floats -> floats[index];
            case double[] doubles -> doubles[index];
            case Object[] objs -> objs[index];
            default -> throw new IllegalArgumentException(array.getClass() + " is not an array!");
        };
    }

    public static int unknownIntToInt16(Object val) {
        return switch(val) {
            case Byte b -> b;
            case Short s -> s;
            case Integer i -> i;
            case Long l -> l.intValue();
            default -> throw new IllegalArgumentException("Unexpected value: " + val);
        };
    }

    public static Object int16ToUnknownInt(int val, PrimitiveType primitive) {
        return switch (primitive) {
            case BYTE -> (byte) val;
            case SHORT -> (short) val;
            case INTEGER -> val;
            case BIGINT -> (long) val;
            default -> throw new IllegalArgumentException("Unexpected value: " + primitive);
        };
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

    // TODO: review methods below for better functionality
    @Debug
    @DoNotCall
    // Returns the class that last called the method
    public static Class<?> getMethodCaller() {
        return getMethodCallerElement(1).getDeclaringClass();
    }

    @Debug
    @DoNotCall
    public static StackWalker.StackFrame getMethodCallerElement(int depth) {
        StackWalker.StackFrame stackFrame = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
                .walk(s -> s.skip(1 + depth).limit(1).findFirst())
                .orElseThrow(() -> new RuntimeException("No method caller"));
        return stackFrame;
    }

    @Debug
    @DoNotCall
    public static StackWalker.StackFrame getMethodCallerElement() {
        return getMethodCallerElement(1);
    }
}
