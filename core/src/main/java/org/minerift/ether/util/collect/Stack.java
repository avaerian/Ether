package org.minerift.ether.util.collect;

import com.google.common.base.Preconditions;
import org.minerift.ether.debug.Debug;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.IntUnaryOperator;

public class Stack<T> /* implements Collection<T>; for later */ {

    // TODO: insertAfter(), Iterable<T>, unit tests

    public static final int DEFAULT_CAPACITY = 16;
    public static final IntUnaryOperator DEFAULT_GROWER = (i) -> i + Math.max(i >> 1, 1); // i * 1.5, almost equivalent

    private IntUnaryOperator grower; // IN: old capacity, OUT: new capacity
    private T[] stack;
    private int size;

    public Stack() {
        this(DEFAULT_CAPACITY);
    }

    public Stack(int capacity) {
        this(capacity, DEFAULT_GROWER);
    }

    public Stack(int capacity, IntUnaryOperator grower) {
        this((T[]) new Object[capacity], 0, grower);
    }

    public Stack(T[] stack, int size) {
        this(stack, size, DEFAULT_GROWER);
    }

    public Stack(T[] stack, int size, IntUnaryOperator grower) {
        this.stack = stack;
        this.size = size;
        this.grower = grower;
    }

    @Debug
    public static void main(String[] args) {
        Stack<String> stack = new Stack<>();
        stack.push("A");
        stack.push("B");
        stack.push("C");
        stack.push("D");

        System.out.println(stack);

        //stack.insert(stack.size() - 2, new String[] { "E", "F", "G" });
        //stack.insert(3, List.of("J", "K", "L"));

        stack.expand(stack.size() - 1, "E", "F", "G");
        stack.expand(3, List.of("J", "K", "L"));

        System.out.println(stack);

        stack.pop();
        stack.pop();

        System.out.println(stack);

        stack.pushAll(new String[] { "X", "Y", "Z" });
        //stack.push("H");
        //stack.push("I");

        System.out.println(stack);

        stack.insert(stack.size - 2, new String[]{":)", ":("});
        stack.insert(stack.size - 3, ":0");

        System.out.println(stack);

        stack.insert(stack.size - 2, List.of(":\\", ":/"));

        System.out.println(stack);

        stack.insert(stack.size - 1, ":0");

        System.out.println(stack);
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public T peek() {
        return isEmpty() ? null : stack[size - 1];
    }

    // Push an element to the top of the stack
    public void push(T element) {
        growIfNeeded(1);
        stack[size++] = element;
    }

    @SafeVarargs // TODO: review annotation
    public final void pushAll(T... elements) {
        growIfNeeded(elements.length);
        System.arraycopy(elements, 0, stack, size, elements.length);
        size += elements.length;
    }

    public void pushAll(Collection<T> elements) {
        growIfNeeded(elements.size());

        int i = 0;
        for(Iterator<T> it = elements.iterator(); it.hasNext(); i++) {
            stack[size++] = it.next();
        }
    }

    public void insert(int idx, T element) {
        Preconditions.checkElementIndex(idx, size);
        growIfNeeded(1);

        System.arraycopy(stack, idx, stack, idx + 1, size - idx);
        stack[idx] = element;
        size++;
    }

    public void insert(int idx, T... elements) {
        Preconditions.checkElementIndex(idx, size);
        growIfNeeded(elements.length);

        if(idx != size - 1) {
            System.arraycopy(stack, idx, stack, idx + elements.length, size - idx);
        }

        T moveToLast = stack[idx];
        System.arraycopy(elements, 0, stack, idx, elements.length);
        stack[idx + elements.length] = moveToLast;
        size += elements.length;
    }

    public void insert(int idx, Collection<T> elements) {
        Preconditions.checkElementIndex(idx, size);
        growIfNeeded(elements.size());

        if(idx != size - 1) {
            System.out.println("Before: " + Arrays.toString(stack));
            System.arraycopy(stack, idx, stack, idx + elements.size(), size - idx);
            System.out.println("After: " + Arrays.toString(stack));
        }

        T moveToLast = stack[idx];
        int i = 0;
        for(Iterator<T> it = elements.iterator(); it.hasNext(); i++) {
            stack[idx + i] = it.next();
        }
        stack[idx + i] = moveToLast;
        size += elements.size();
    }

    public void clear() {
        Arrays.fill(stack, 0, size, null);
        this.size = 0;
    }

    @SuppressWarnings("Duplicates")
    public void expand(int idx, T... elements) {
        Preconditions.checkElementIndex(idx, size);
        growIfNeeded(elements.length);

        if(idx != size - 1) {
            System.arraycopy(stack, idx + 1, stack, idx + elements.length, size - idx - 1);
            size--;
        }

        System.arraycopy(elements, 0, stack, idx, elements.length);
        size += elements.length - 1;
    }

    @SuppressWarnings("Duplicates")
    public void expand(int idx, Collection<T> elements) {
        Preconditions.checkElementIndex(idx, size);
        growIfNeeded(elements.size());

        if(idx != size - 1) {
            System.arraycopy(stack, idx + 1, stack, idx + elements.size(), size - idx - 1);
            size--;
        }

        int i = 0;
        for(Iterator<T> it = elements.iterator(); it.hasNext(); i++) {
            stack[idx + i] = it.next();
        }
        size += elements.size() - 1;
    }

    public T get(int idx) {
        return stack[idx];
    }

    public T getFromTop(int idx) {
        return stack[size - 1 - idx];
    }

    public void pop() {
        stack[--size] = null;
    }

    private void growIfNeeded(int elements) {
        if(size + elements >= capacity()) {
            grow();
        }
    }

    private void grow() {
        int capacity = grower.applyAsInt(capacity());
        T[] grown = (T[]) new Object[capacity];
        System.arraycopy(stack, 0, grown, 0, size);
        this.stack = grown;
    }

    public int capacity() {
        return stack.length;
    }

    public int size() {
        return size;
    }

    @Override
    public String toString() {
        return "SchemaParserStack{" +
                //"grower=" + grower +
                "stack=" + Arrays.toString(Arrays.copyOf(stack, size)) +
                ", size=" + size +
                '}';
    }
}
