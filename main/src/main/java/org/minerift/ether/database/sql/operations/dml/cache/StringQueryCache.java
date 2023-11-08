package org.minerift.ether.database.sql.operations.dml.cache;

public class StringQueryCache extends QueryCache {

    private String sql;

    public StringQueryCache(String sql) {
        this.sql = sql;
    }

    public String getQuery() {
        return sql;
    }

}
