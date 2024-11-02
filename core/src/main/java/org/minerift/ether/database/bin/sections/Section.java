package org.minerift.ether.database.bin.sections;

import org.jetbrains.annotations.Nullable;
import org.minerift.ether.database.DataType;
import org.minerift.ether.database.bin.Ref;
import org.minerift.ether.util.Utils;

import java.util.LinkedList;

import static org.minerift.ether.util.Utils.getArrayElement;
import static org.minerift.ether.util.Utils.unknownIntToInt16;

public abstract class Section {

    public static final int VALUE_NOT_SET = -1;

    protected final String name;
    protected LinkedList<DataEntry> dataEntries;
    protected int sectionSizeEntryIndex;
    //protected DataEntry<int[]> offsetsEntry;
    protected Runnable lazyEntryInit;

    public Section() {
        this(null, null);
    }

    public Section(String name) {
        this(name, null);
    }

    public <T> Section(String name, DataType<T> type, Ref<T> ref) {
        this(name, new DataEntry<>(type, ref, null));
    }

    public Section(String name, DataEntry sectionSize) {
        this.name = name;
        this.dataEntries = new LinkedList<>();
        if(sectionSize != null) {
            dataEntries.add(sectionSize);
            this.sectionSizeEntryIndex = 0;
        } else {
            this.sectionSizeEntryIndex = VALUE_NOT_SET;
        }
        this.lazyEntryInit = null;
    }

    public Section(DataEntry sectionSize) {
        this(null, sectionSize);
    }

    public void contents(Runnable lazyEntryInit) {
        this.lazyEntryInit = lazyEntryInit;
    }

    public void loadFields() {
        lazyEntryInit.run();
    }

    protected <T> Section add(DataType<T> type, Ref<T> data, Ref arraySize) {
        DataEntry<T> entry = new DataEntry<>(type, data, arraySize);
        // TODO: resolve
        /*if(where == AppendType.PREPEND) {
            if(sectionSizeEntryIndex == VALUE_NOT_SET) {
                throw new UnsupportedOperationException("Attempted to prepend, but section size entry was not set");
            }
            dataEntries.add(sectionSizeEntryIndex++, entry);
        } else {
            dataEntries.add(entry);
        }*/

        onAdd
        return this;
    }

    /*public <T> Section add(DataType<T> type, Ref<T> data, Ref arraySize) {
        return add(type, data, arraySize, AppendType.APPEND);
    }*/

    public <T> Section add(DataType<T> type, Ref<T> data) {
        return add(type, data, null);
    }

    public boolean isNamed() {
        return name != null && !name.isBlank();
    }

    /*public <T> void addSectionSize(DataType<T> type, Ref<T> data) {
        if(sectionSizeEntryIndex != VALUE_NOT_SET) {
            throw new UnsupportedOperationException("Section size entry has already been added to " + (isNamed() ? "unnamed section" : name));
        }
        this.sectionSizeEntryIndex = dataEntries.size();
        add(type, data, null); // section size should not be an array type
    }*/

    public DataEntry getSectionSizeEntry() {
        return sectionSizeEntryIndex != VALUE_NOT_SET ? dataEntries.get(sectionSizeEntryIndex) : null;
    }

    public boolean isFixedSize() {
        for(DataEntry entry : dataEntries) {
            if(entry.isDynamicallySized()) {
                return false;
            }
        }
        return true;
    }

    public int getByteSize() {
        int size = 0;
        int i = 0;
        for(var it = dataEntries.iterator(); it.hasNext(); i++) {
            if(i < sectionSizeEntryIndex) {
                continue;
            }
            DataEntry entry = it.next();
            size += entry.getByteSize();
        }
        return size;
    }

    public abstract Section getSubsection(String name);
    public abstract Section getSubsection(int idx);

    /**
     * Add a subsection to this section.
     * @return this section, not the newly added one, for chained statements.
     */

    public abstract Section addSubsection(Section subsection);

    public static class DataEntry<T> {
        public final DataType<T> type;
        public final Ref<T> data;
        public final Ref arraySize;

        public DataEntry(DataType<T> type, Ref<T> data, @Nullable Ref arraySize) {
            this.type = type;
            this.data = data;
            this.arraySize = arraySize;
        }

        public boolean isArray() {
            return type.isArrayType() && !type.isExtendedBinaryType();
        }

        public boolean hasDynamicArrayLength() {
            return arraySize == null && type.hasDynamicArrayLength();
        }

        public boolean isDynamicallySized() {
            return (isArray() && hasDynamicArrayLength()) || type.isDynamicallySizedType();
        }

        public int getArrayLength() {
            if(!type.isArrayType()) {
                throw new RuntimeException("Data type is not an array of " + type.getPrimitiveType());
            }
            if(hasDynamicArrayLength()) {
                return Utils.getArrayLength(data.get());
            }
            return arraySize != null ? unknownIntToInt16(arraySize.get()) : type.arrayLength();
        }

        public int getByteSize() {
            if(!isArray()) {
                return type.getByteSize(data);
            }

            int size = getArrayLength();
            int bytes = hasDynamicArrayLength() ? Integer.BYTES : 0;
            Ref element = new Ref();
            for(int i = 0; i < size; i++) {
                element.set(getArrayElement(data.get(), i));
                bytes += type.getByteSize(element);
            }
            return bytes;
        }
    }

}
