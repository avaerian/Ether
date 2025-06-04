package org.minerift.ether.database.bin.sections;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.UnaryOperator;

@Deprecated
public class LinkedList<T> implements List<T>, Deque<T> {

    protected int size;
    protected Node<T> head;
    protected Node<T> tail;

    public LinkedList() {
        this.size = 0;
        this.head = null;
        this.tail = null;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean contains(Object o) {
        for(Node<T> node = head; node.hasNext(); ) {
            if(node.element.equals(o)) {
                return true;
            }
            node = node.nextNode;
        }
        return false;
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return null;
    }

    @NotNull
    @Override
    public Iterator<T> descendingIterator() {
        return null;
    }

    @NotNull
    @Override
    public Object[] toArray() {
        return new Object[0];
    }

    @NotNull
    @Override
    public <T1> T1[] toArray(@NotNull T1[] a) {
        return null;
    }

    @Override
    public boolean offerFirst(T t) {
        return false;
    }

    @Override
    public boolean offerLast(T t) {
        return false;
    }

    @Override
    public T pollFirst() {
        return null;
    }

    @Override
    public T pollLast() {
        return isEmpty() ? null : poll();
    }

    @Override
    public T peekFirst() {
        return null;
    }

    @Override
    public T peekLast() {
        return null;
    }

    @Override
    public boolean removeFirstOccurrence(Object o) {
        return false;
    }

    @Override
    public boolean removeLastOccurrence(Object o) {
        return false;
    }

    @Override
    public boolean add(T t) {
        tail.add(new Node<>(t));
        this.tail = tail.nextNode;
        return true;
    }

    @Override
    public boolean offer(T t) {
        return false;
    }

    @Override
    public T remove() {
        if(isEmpty()) {
            throw new NoSuchElementException();
        }
        T element = tail.element;
        Node<T> oldTail = tail;
        this.tail = tail.prevNode;
        oldTail.remove();
        return element;
    }

    @Override
    public T poll() {
        return null;
    }

    @Override
    public T element() {
        if(isEmpty()) {
            throw new NoSuchElementException();
        }
        return head.element;
    }

    @Override
    public T peek() {
        return isEmpty() ? null : head.element;
    }

    @Override
    public boolean remove(Object o) {
        return false;
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return false;
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends T> c) {
        return false;
    }

    @Override
    public void push(T t) {

    }

    @Override
    public T pop() {
        return null;
    }

    @Override
    public boolean addAll(int index, @NotNull Collection<? extends T> c) {
        return false;
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        return false;
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        return false;
    }

    @Override
    public void replaceAll(UnaryOperator<T> operator) {
        List.super.replaceAll(operator);
    }

    @Override
    public void clear() {
        for(Node<T> node = head; node.hasNext(); ) {
            Node<T> next = node.nextNode;
            node.element = null;
            node.nextNode = null;
            node.prevNode = null;
            node = next;
        }
        this.size = 0;
    }

    private void ensureValidIndex(int index) {
        if(index > size) {
            throw new IndexOutOfBoundsException();
        }
    }

    @Override
    public T get(int index) {
        return getNode(index).get();
    }

    public Node<T> getNode(int index) {
        ensureValidIndex(index);
        Node<T> node;
        if(index < (size >> 1)) {
            node = head;
            for(int i = 0; i < index; i++) {
                node = node.nextNode;
            }
        } else {
            node = tail;
            for(int i = size; i > index; i--) {
                node = node.prevNode;
            }
        }
        return node;
    }

    @Override
    public T set(int index, T element) {
        return getNode(index).set(element);
    }

    @Override
    public void add(int index, T element) {
        Node<T> node = getNode(index);
        node.add(new Node<>(element));
    }

    @Override
    public T remove(int index) {
        return null;
    }

    @Override
    public int indexOf(Object o) {
        return 0;
    }

    @Override
    public int lastIndexOf(Object o) {
        return 0;
    }

    @NotNull
    @Override
    public ListIterator<T> listIterator() {
        return null;
    }

    @NotNull
    @Override
    public ListIterator<T> listIterator(int index) {
        return null;
    }

    @NotNull
    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        return null;
    }

    @Override
    public Spliterator<T> spliterator() {
        return List.super.spliterator();
    }

    @Override
    public void addFirst(T t) {
        List.super.addFirst(t);
    }

    @Override
    public void addLast(T t) {
        List.super.addLast(t);
    }

    @Override
    public T getFirst() {
        return head.element;
    }

    @Override
    public T getLast() {
        return List.super.getLast();
    }

    @Override
    public T removeFirst() {
        return List.super.removeFirst();
    }

    @Override
    public T removeLast() {
        return List.super.removeLast();
    }

    @Override
    public LinkedList<T> reversed() {
        return null;
    }

    public static class Node<T> {
        protected T element;
        protected Node<T> nextNode;
        protected Node<T> prevNode;

        public Node(T element) {
            this(element, null, null);
        }

        public Node(T element, Node<T> next, Node<T> prev) {
            this.element = element;
            this.nextNode = next;
            this.prevNode = prev;
        }

        public boolean isOrphan() {
            return nextNode == null && prevNode == null;
        }

        public boolean hasNext() {
            return nextNode != null;
        }

        public boolean hasBefore() {
            return prevNode != null;
        }

        public T get() {
            return element;
        }

        public T set(T element) {
            T prev = this.element;
            this.element = element;
            return prev;
        }

        public void add(Node<T> nextNode) {
            nextNode.prevNode = this;
            this.nextNode = nextNode;
        }

        public void addBefore(Node<T> beforeNode) {
            beforeNode.nextNode = this;
            this.prevNode = beforeNode;
        }

        public void remove() {
            if(hasBefore()) {
                this.prevNode.nextNode = nextNode;
            }
            if(hasNext()) {
                this.nextNode.prevNode = prevNode;
            }
            this.element = null;
        }

    }

}
