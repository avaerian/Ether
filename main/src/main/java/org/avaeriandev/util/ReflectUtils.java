package org.avaeriandev.util;

import java.lang.reflect.Field;

@Deprecated
public class ReflectUtils {

    // Bypasses final modifier
    public static void setFieldUnchecked(Object object, String field, Object newData) throws NoSuchFieldException, IllegalAccessException {
        Field f = object.getClass().getDeclaredField(field);
        f.setAccessible(true);
        f.set(object, newData);
    }

}
