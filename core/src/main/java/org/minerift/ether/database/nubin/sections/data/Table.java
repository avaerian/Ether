package org.minerift.ether.database.nubin.sections.data;

import org.minerift.ether.database.bin.Ref;

public class Table {
    public Ref<Integer> size;
    public String name;
    public Ref<Integer> fieldCount;
    public Ref<BinaryField[]> fields;
    public Ref<Integer> btreesCount;
    public Ref<BinaryBTreeInfo[]> btrees;
    public Ref<Integer> entryCount;
    public Ref<Integer> entryStart;
    public Ref<int[]> entryOffsets;

    public Table() {
        this.size = new Ref<>();
        this.name = null;
        this.fieldCount = new Ref<>();
        this.fields = new Ref<>();
        this.btreesCount = new Ref<>();
        this.btrees = new Ref<>();
        this.entryCount = new Ref<>();
        this.entryStart = new Ref<>();
        this.entryOffsets = new Ref<>();
    }

    public static class BinaryField {

        public Ref<String> name;
        public Ref<Byte> type;
        public Ref<Byte> flags; // is array, allows nulls,
        public Ref<Integer> nullBitmapSize;
        public Ref<long[]> nullBitmap;
    }

    public static class BinaryBTreeInfo {

    }
}
