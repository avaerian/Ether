package org.minerift.ether.util.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;

public class Reflect {

    // TODO: search API for reflection abstraction
    /*
        reflectedObj.search().
            .getFieldsFromRefs(getFields)
            .ofType(Field.class)
            //.ofTypes()
            .withAnnotation(PrimaryKey.class)
            //.withAnnotations()
            .find();

     */




    // OBJECT INSTANCES
    public static <T> ReflectedObject<T> of(T obj) {
        return new ReflectedObject<>(obj);
    }

    public static <T> ReflectedObject<T>[] of(Collection<T> objs) {
        ReflectedObject<T>[] reflectedObjs = new ReflectedObject[objs.size()];
        int i = 0;
        for(T obj : objs) {
            reflectedObjs[i] = of(obj);
            i++;
        }
        return reflectedObjs;
    }

    public static <T> ReflectedObject<T>[] of(T ... objs) {
        ReflectedObject<T>[] reflectedObjs = new ReflectedObject[objs.length];
        for(int i = 0; i < reflectedObjs.length; i++) {
            reflectedObjs[i] = of(objs[i]);
        }
        return reflectedObjs;
    }




    // CLASSES
    public static <T> ReflectedClass<T> of(Class<T> cls) {
        return new ReflectedClass<>(cls);
    }




    // FIELDS
    public static ReflectedField of(Field field) {
        return new ReflectedField(field);
    }

    public static ReflectedFields of(Field[] fields) {
        final ReflectedField[] reflectedFields = new ReflectedField[fields.length];
        for(int i = 0; i < fields.length; i++) {
            reflectedFields[i] = of(fields[i]);
        }
        return new ReflectedFields(reflectedFields);
    }




    // METHODS
    public static ReflectedMethod of(Method method) {
        return new ReflectedMethod(method);
    }

    public static ReflectedMethods of(Method[] methods) {
        final ReflectedMethod[] reflectedMethods = new ReflectedMethod[methods.length];
        for(int i = 0; i < reflectedMethods.length; i++) {
            reflectedMethods[i] = of(methods[i]);
        }
        return new ReflectedMethods(reflectedMethods);
    }


}
