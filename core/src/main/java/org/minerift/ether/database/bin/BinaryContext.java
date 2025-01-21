package org.minerift.ether.database.bin;

import org.minerift.ether.database.DataType;
import org.minerift.ether.database.Ref;

public abstract class BinaryContext {

    public <T> void bin(DataType<T> type, Ref<T> val) {
        bin(type, val, null);
    }

    public abstract <T> void bin(DataType<T> type, Ref<T> val, Ref arraySize);
    public abstract void markSectionSize(); // marks the next bin() as a section size
    public abstract void beginSection(String name);
    public abstract void markSectionDone();
    public abstract void markComplete();
    public abstract boolean isWriter();
    public abstract boolean isReader();
    public abstract int getSectionSize();
    public abstract void allowNulls(boolean nullable);

}
