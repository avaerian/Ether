package org.minerift.ether.database.metadata;

import org.minerift.ether.database.DataType;
import org.minerift.ether.database.Record;
import org.minerift.ether.database.Result;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

// Acts as a wrapper around a Result to retrieve query-specific details
public class TableSnapshot {

    private static final String TABLE_NAME = "table_name";
    private static final String COL_NAME = "column_name";

    private final Result<?> columns;
    private final Result<?> indexes;
    private Map<String, DataType<?>> fieldTypes;
    private String tableName;


    public TableSnapshot(Result<?> columns, Result<?> indexes) {
        this.columns = columns;
        this.indexes = indexes;
        if(!columns.isEmpty()) {
            this.tableName = columns.getRecord(0).get(TABLE_NAME, String.class);

            this.fieldTypes = new HashMap<>(columns.size());
            for(Record<?> record : columns) {
                DataType<?> type = DataType.VARCHAR; // TODO: for getting appropriate type, enums need to be handled by checking indexes

                fieldTypes.put(record.get(COL_NAME, String.class), null);
            }
        } else {
            this.fieldTypes = Collections.emptyMap();
            this.tableName = null;
        }
    }

    public String getTableName() {
        return tableName;
    }

    public Result<?> asResult() {
        return columns;
    }

}
