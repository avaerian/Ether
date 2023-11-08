package org.minerift.ether.util.reflect;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.Predicate;

public class ReflectedFields implements Iterable<ReflectedField> {

    private final ReflectedField[] fields;

    public ReflectedFields(ReflectedField[] fields) {
        this.fields = fields;
    }

    public ReflectedField[] getArray() {
        return fields;
    }

    public boolean hasAnnotation(Class<? extends Annotation> ann) {
        return all(field -> field.hasAnnotation(ann));
    }

    public boolean hasAnnotations(Collection<Class<? extends Annotation>> anns) {
        return all(field -> field.hasAnnotations(anns));
    }

    public boolean hasAnnotations(Class<? extends Annotation> ann, Class<? extends Annotation> ... rest) {
        return all(field -> field.hasAnnotations(ann, rest));
    }

    public boolean all(Predicate<ReflectedField> condition) {
        for(ReflectedField field : fields) {
            if(!condition.test(field)) {
                return false;
            }
        }
        return true;
    }

    public ReflectedFields filter(Predicate<ReflectedField> condition) {
        return new ReflectedFields(Arrays.stream(fields).filter(condition).toArray(ReflectedField[]::new));
    }

    public Object[] readAll(Object holder) {
        Object[] vals = new Object[fields.length];
        for(int i = 0; i < vals.length; i++) {
            vals[i] = fields[i].get(holder);
        }
        return vals;
    }

    public <T> T[] readAllTyped(Object holder, Class<T> type) {
        T[] vals = (T[]) Array.newInstance(type, fields.length);
        for(int i = 0; i < vals.length; i++) {
            vals[i] = (T) fields[i].get(holder);
        }
        return vals;
    }

    @NotNull
    @Override
    public Iterator<ReflectedField> iterator() {
        return Arrays.stream(fields).iterator();
    }
}
