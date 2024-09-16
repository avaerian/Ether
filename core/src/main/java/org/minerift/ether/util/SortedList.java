package org.minerift.ether.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

// TODO: fully develop this class (ensure other methods work properly as well in the future)
public class SortedList<T> extends ArrayList<T> {

    private Comparator<T> comparator;

    public SortedList(Comparator<T> comparator) {
        this.comparator = comparator;
    }

    @Override
    public boolean add(T t) {
        int index = Collections.binarySearch(this, t, this.comparator);
        if (index < 0) index = ~index;

        super.add(index, t);
        return true;
    }
}
