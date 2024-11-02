package org.minerift.ether.database.bin.sections;

import org.minerift.ether.database.bin.Ref;

public class Header {
    public Ref<byte[]> magic;
    public Ref<String> dbName;
    public Ref<Short> size;
    public Ref<Short> tableCount;
    public Ref<String[]> tableNames;
    public Ref<int[]> tableOffsets;

    public Header() {
        this.magic = new Ref<>();
        this.dbName = new Ref<>();
        this.size = new Ref<>();
        this.tableCount = new Ref<>();
        this.tableNames = new Ref<>();
        this.tableOffsets = new Ref<>();
    }
}
