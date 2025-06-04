package org.minerift.ether.database.nusql;

import org.minerift.ether.database.Field;
import org.minerift.ether.database.Model;
import org.minerift.ether.database.Record;

import static org.minerift.ether.database.nusql.SQLUtils.asJooqField;

public class SQLRecord<MO> extends Record<MO> {

    private final org.jooq.Record jooqRecord;

    public SQLRecord(Model<MO, ?> model, org.jooq.Record record) {
        super(model);
        this.jooqRecord = record;
    }

    @Override
    public <T> T get(Field<?, T, ?> field) {
        Object sqlVal = jooqRecord.get(asJooqField(field));
        //System.out.println(this.getClass() + " : get() sqlVal: " + sqlVal); // debug
        return field.readSQLAsJavaValue(sqlVal);
    }

    @Override
    public <C, T> C get(Field.FieldWithAdapter<?, C, T, ?> field) {
        return field.adapter.adaptFrom(get((Field<?, T, ?>) field));
    }

    @Override
    public Object get(String column) {
        return jooqRecord.get(column);
    }

    @Override
    public Object get(int idx) {
        return jooqRecord.get(idx);
    }

    public org.jooq.Record asJooqRecord() {
        return jooqRecord;
    }
}
