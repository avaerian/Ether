package org.minerift.ether.util.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectedMethod implements IReflectedElement {

    private final Method method;

    public ReflectedMethod(Method method) {
        this.method = method;
    }

    public <T> T invoke(Object obj, Object ... args) {
        try {
            method.setAccessible(true);
            return (T) method.invoke(obj, args);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            throw new RuntimeException(ex);
        }
    }

    public Class<?> getReturnType() {
        return method.getReturnType();
    }

    public Class<?>[] getExceptionTypes() {
        return method.getExceptionTypes();
    }

    @Override
    public boolean hasAnnotation(Class<? extends Annotation> ann) {
        return method.isAnnotationPresent(ann);
    }

    @Override
    public Annotation[] getAnnotations() {
        return method.getAnnotations();
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> clazz) {
        return method.getAnnotation(clazz);
    }
}
