package org.minerift.ether.database.sql.meta;

import org.minerift.ether.util.pair.Pair;

import java.util.HashMap;
import java.util.Map;

public class Table {
    public String tableName;
    public String primaryKey;
    public boolean isTemp;
    public Map<String, ColumnInfo> columns = new HashMap<>();
    public Map<String, Pair<String, String>> childCol2ParentModel = new HashMap<>(4); // local column REFERENCES (parent model, parent column)

    @Override
    public String toString() {
        return "Table{" +
                "tableName='" + tableName + '\'' +
                ", primaryKey='" + primaryKey + '\'' +
                ", isTemp=" + isTemp +
                ", cols=" + columns +
                ", childCol2ParentModel=" + childCol2ParentModel +
                '}';
    }
}
