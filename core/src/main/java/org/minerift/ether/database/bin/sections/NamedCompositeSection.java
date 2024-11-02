package org.minerift.ether.database.bin.sections;

import org.minerift.ether.database.DataType;
import org.minerift.ether.database.bin.Ref;
import org.minerift.ether.util.UnreachableException;

import java.util.LinkedHashMap;
import java.util.Map;

public class NamedCompositeSection extends Section {

    protected Map<String, Section> sectionsByName;

    public NamedCompositeSection() {
    }

    public NamedCompositeSection(String name) {
        super(name);
    }

    public <T> NamedCompositeSection(String name, DataType<T> type, Ref<T> ref) {
        super(name, type, ref);
        this.sectionsByName = new LinkedHashMap<>();
    }

    public NamedCompositeSection(String name, DataEntry sectionSize) {
        super(name, sectionSize);
    }

    public NamedCompositeSection(DataEntry sectionSize) {
        super(sectionSize);
    }

    @Override
    public Section addSubsection(Section subsection) {
        if(sectionsByName.containsKey(subsection.name)) {
            // TODO: log
        }
        return sectionsByName.put(subsection.name, subsection);
    }

    @Override
    public Section getSubsection(String name) {
        return sectionsByName.get(name);
    }

    @Override
    public Section getSubsection(int idx) {
        if(idx < 0 || idx >= sectionsByName.size()) {
            throw new IndexOutOfBoundsException();
        }
        int i = 0;
        for(Map.Entry<String, Section> entry : sectionsByName.entrySet()) {
            if(i++ == idx) {
                return entry.getValue();
            }
        }
        throw new UnreachableException();
    }
}
