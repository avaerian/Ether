package org.minerift.ether.database.bin.depbin.sections.data;

import org.minerift.ether.database.Field;
import org.minerift.ether.database.Model;
import org.minerift.ether.database.Ref;

public class TableMetadata {
    public Ref<Integer> size;
    public String name;
    public Ref<Integer> fieldCount;
    public Ref<BinaryField[]> fields;
    public Ref<Integer> btreesCount;
    public Ref<BinaryBTreeInfo[]> btrees;
    public Ref<Integer> entryCount;
    public Ref<Integer> entryStart;
    public Ref<int[]> entryOffsets;

    public TableMetadata() {
        this.size = new Ref<>(0);
        this.name = null;
        this.fieldCount = new Ref<>();
        this.fields = new Ref<>();
        this.btreesCount = new Ref<>();
        this.btrees = new Ref<>();
        this.entryCount = new Ref<>();
        this.entryStart = new Ref<>();
        this.entryOffsets = new Ref<>();
    }

    public TableMetadata(Model<?, ?> model) {
        this.size = new Ref<>(0);
        this.name = model.getTableName();
        this.fieldCount = new Ref<>(model.getFields().size());
        BinaryField[] binFields = new BinaryField[model.getFields().size()];
        // TODO: binFields[0] = new BinaryField(model.getPrimaryKey());
    }

    public static class BinaryField {

        public static final byte PRIMARY_KEY = 1; // 1 << 0
        public static final byte ALLOWS_NULLS = 2; // 1 << 1
        public static final byte ARRAY_TYPE = 4; // 1 << 2


        public Ref<Integer> size;
        public String name;
        public byte type;
        public byte flags; // is array, allows nulls,
        public Ref<Integer> nullBitmapSize;
        public long[] nullBitmap; // TODO: should be BitSet?

        public BinaryField() {

        }

        public <MO> BinaryField(Model<MO, ?> model, Field<MO, ?, ?> field) {
            this.name = field.getName();
            this.type = (byte) field.getDataType().getPrimitiveType().ordinal(); // TODO: change this

            this.flags = 0;
            if(model.getPrimaryKey() == field) {
                this.flags |= PRIMARY_KEY;
            }
            if(field.getDataType().isNullable()) {
                this.flags |= ALLOWS_NULLS;
            }
        }
    }

    public static class BinaryBTreeInfo {

    }
}
