package org.minerift.ether.work;

public class SingleWork<T> extends Work<T> {

    public SingleWork(String name) {
        super(name);
    }

    @Override
    public boolean complete() {
        return false;
    }
}
