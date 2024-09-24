package org.minerift.ether.util.reflect;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Predicate;

public class ReflectedMethods implements Iterable<ReflectedMethod> {

    private final ReflectedMethod[] methods;

    public ReflectedMethods(ReflectedMethod[] methods) {
        this.methods = methods;
    }

    // Creates a new batch of reflected methods, retaining only the elements that test true
    public ReflectedMethods filter(Predicate<ReflectedMethod> condition) {
        return new ReflectedMethods(Arrays.stream(methods).filter(condition).toArray(ReflectedMethod[]::new));
    }

    public ReflectedMethod[] getArray() {
        return methods;
    }

    @NotNull
    @Override
    public Iterator<ReflectedMethod> iterator() {
        return Arrays.stream(methods).iterator();
    }

    private boolean all(Predicate<ReflectedMethod> condition) {
        for(ReflectedMethod method : methods) {
            if(!condition.test(method)) {
                return false;
            }
        }
        return true;
    }
}
