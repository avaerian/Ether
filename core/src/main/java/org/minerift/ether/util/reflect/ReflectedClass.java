package org.minerift.ether.util.reflect;

import com.google.common.primitives.Primitives;
import org.minerift.ether.debug.Debug;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Deprecated
public class ReflectedClass<T> implements IReflectedElement {

    @Debug
    public static class TestCls {
        public final int TEST_PK = 42069;
        public final int TEST2 = 42069;

    }

    @Debug
    public static void main(String[] args) {
        TestCls test = new TestCls();
        System.out.println(Reflect.of(test).getFieldFromRef(test.TEST_PK).getName());
    }

    private final Class<T> cls;

    public ReflectedClass(Class<T> cls) {
        this.cls = cls;
    }

    @Override
    public boolean hasAnnotation(Class<? extends Annotation> ann) {
        return cls.isAnnotationPresent(ann);
    }

    @Override
    public boolean hasAnnotations(Collection<Class<? extends Annotation>> anns) {
        return anns.containsAll(getAnnotationClasses());
    }

    @Override
    public Annotation[] getAnnotations() {
        return cls.getAnnotations();
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> clazz) {
        return cls.getAnnotation(clazz);
    }

    public boolean isPrimitive() {
        return cls.isPrimitive();
    }

    public boolean isBoxedPrimitive() {
        return Primitives.isWrapperType(cls);
    }

    public boolean isArray() {
        return cls.isArray();
    }

    public Class<?> getArrayType() {
        return cls.arrayType();
    }

    public boolean isAnnotationClass() {
        return cls.isAnnotation();
    }

    public ReflectedFields getPublicFields() {
        return Reflect.of(cls.getFields());
    }

    public ReflectedFields getFields() {
        return Reflect.of(cls.getDeclaredFields());
    }

    public ReflectedField getField(String name) throws NoSuchFieldException {
        return Reflect.of(cls.getDeclaredField(name));
    }

    public ReflectedField getFieldOrNull(String name) {
        try {
            return getField(name);
        } catch (NoSuchFieldException ex) {
            return null;
        }
    }

    public ReflectedMethods getMethods() {
        return Reflect.of(cls.getMethods());
    }

    public ReflectedMethods getPublicMethods() {
        return Reflect.of(cls.getDeclaredMethods());
    }

    // TODO: is there a better way for this code not be as brute force?
    public Map<ReflectedField, Object> mapFieldsToValues(T holder, Iterable<Object> queriedFields) {
        final Map<ReflectedField, Object> fieldsToValues = new HashMap<>();
        // For each field, compare queried field addresses
        for(ReflectedField field : getFields()) {
            for(Object queriedField : queriedFields) {
                Object val = field.getValue(holder);
                if(val == queriedField) {
                    fieldsToValues.put(field, val);
                    break;
                }
            }
        }
        return fieldsToValues;
    }

    public Map<ReflectedField, Object> mapFieldsToValues(T holder) {
        final Map<ReflectedField, Object> fieldsToValues = new HashMap<>();
        for(ReflectedField field : getFields()) {
            Object val = field.getValue(holder);
            fieldsToValues.put(field, val);
        }
        return fieldsToValues;
    }
}
