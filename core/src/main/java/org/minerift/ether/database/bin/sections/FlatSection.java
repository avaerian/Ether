package org.minerift.ether.database.bin.sections;

import org.minerift.ether.database.DataType;
import org.minerift.ether.database.Ref;

public class FlatSection extends Section {

    public FlatSection(String name) {
        super(name);
    }

    public <T> FlatSection(String name, DataType<T> type, Ref<T> ref) {
        super(name, type, ref);
    }

    public FlatSection(String name, DataEntry sectionSize) {
        super(name, sectionSize);
    }

    public FlatSection(DataEntry sectionSize) {
        super(sectionSize);
    }

    public FlatSection() {
    }

    /*@Override
    public <T> void add(DataType<T> type, Ref<T> data, Ref arraySize) {

    }*/

    @Override
    public boolean isFixedSize() {
        return false;
    }

    @Override
    public int getByteSize() {
        return 0;
    }

    @Override
    public Section getSubsection(String name) {
        throw new UnsupportedOperationException("Flat section unable to add sections");
    }

    @Override
    public Section getSubsection(int idx) {
        throw new UnsupportedOperationException("Flat section unable to add sections");
    }

    @Override
    public Section addSubsection(Section subsection) {
        return null;
    }
}
