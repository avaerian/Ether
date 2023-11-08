package org.minerift.ether.util.reflect;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.lang.annotation.Annotation;
import java.util.Collection;

public class ReflectedClass<T> implements IReflectedElement {

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

    public boolean isAnnotationClass() {
        return cls.isAnnotation();
    }

    public ReflectedFields getPublicFields() {
        return Reflect.of(cls.getFields());
    }

    public ReflectedFields getFields() {
        return Reflect.of(cls.getDeclaredFields());
    }

    public ReflectedMethods getMethods() {
        return Reflect.of(cls.getMethods());
    }

    public ReflectedMethods getPublicMethods() {
        return Reflect.of(cls.getDeclaredMethods());
    }

    public BiMap<ReflectedField, Object> mapFieldsToValues(T holder, Iterable<Object> queriedFields) {
        final BiMap<ReflectedField, Object> fieldsToValues = HashBiMap.create();
        // For each field, compare queried field addresses
        for(ReflectedField field : getFields()) {
            for(Object queriedField : queriedFields) {
                if(field.get(holder) == queriedField) {
                    fieldsToValues.put(field, queriedField);
                    break;
                }
            }
        }
        return fieldsToValues;
    }

    // Will not map null fields
    public BiMap<ReflectedField,
            Object> mapFieldsToValues(T holder) {
        final BiMap<ReflectedField, Object> fieldsToValues = HashBiMap.create();
        for(ReflectedField field : getFields()) {
            Object val = field.get(holder);
            if(val != null) {
                fieldsToValues.put(field, val);
            }
        }
        return fieldsToValues;
    }

}
