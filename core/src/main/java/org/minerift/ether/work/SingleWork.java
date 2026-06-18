package org.minerift.ether.work;

public class SingleWork<T> extends Work<T> {



    @Override
    public boolean complete() {
        return false;
    }
}
