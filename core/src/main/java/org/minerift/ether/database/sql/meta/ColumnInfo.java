package org.minerift.ether.database.sql.meta;

import org.minerift.ether.debug.Debug;

import java.util.ArrayList;
import java.util.List;

@Debug
public class ColumnInfo {
    public String name;
    public String type;
    public String order; // ASC, DESC
    public String collation;
    public List<String> checks;
    public OnConflict onConflict;
    public boolean notNull;
    public boolean unique;
    public boolean autoincrement;

    public ColumnInfo() {
        this.checks = new ArrayList<>();
    }

    @Override
    public String toString() {
        return "ColumnInfo{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", notNull=" + notNull +
                ", unique=" + unique +
                ", checks=" + checks +
                '}';
    }
}
