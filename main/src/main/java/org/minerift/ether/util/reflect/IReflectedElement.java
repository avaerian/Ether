package org.minerift.ether.util.reflect;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public interface IReflectedElement {

    boolean hasAnnotation(Class<? extends Annotation> ann);

    Annotation[] getAnnotations();

    <T extends Annotation> T getAnnotation(Class<T> clazz);

    default boolean hasAnnotations(Collection<Class<? extends Annotation>> anns) {
        return getAnnotationClasses().containsAll(anns);
    }

    default Set<Class<? extends Annotation>> getAnnotationClasses() {
        return Arrays.stream(getAnnotations()).map(Annotation::annotationType).collect(Collectors.toSet());
    }

}
