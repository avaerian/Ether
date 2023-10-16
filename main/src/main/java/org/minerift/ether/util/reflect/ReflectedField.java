package org.minerift.ether.util.reflect;

import org.minerift.ether.database.sql.model.PrimaryKey;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;

// Custom abstraction for simplicity
public class ReflectedField implements IReflectedElement {

    @PrimaryKey
    @Deprecated
    private final Field field;

    public static void main(String[] args) throws NoSuchFieldException {
        final var reflectedField = new ReflectedField(ReflectedField.class.getDeclaredField("field"));
        System.out.println(reflectedField.hasAnnotations(PrimaryKey.class, Deprecated.class));
    }

    public ReflectedField(Field field) {
        this.field = field;
    }

    public String getName() {
        return field.getName();
    }

    @Override
    public boolean hasAnnotation(Class<? extends Annotation> ann) {
        return field.isAnnotationPresent(ann);
    }

    public boolean hasAnnotations(Class<? extends Annotation> ann, Class<? extends Annotation> ... rest) {
        Class<? extends Annotation>[] queriedAnns = new Class[rest.length + 1];
        queriedAnns[0] = ann;
        System.arraycopy(rest, 0, queriedAnns, 1, rest.length);
        return hasAnnotations(List.of(queriedAnns));
    }

    @Override
    public Annotation[] getAnnotations() {
        return field.getAnnotations();
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> clazz) {
        return field.getAnnotation(clazz);
    }

    public boolean isAssignableFrom(Class<?> clazz) {
        return field.getType().isAssignableFrom(clazz);
    }

    public Object get(Object obj) {
        try {
            field.setAccessible(true);
            return field.get(obj);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
    }

}
