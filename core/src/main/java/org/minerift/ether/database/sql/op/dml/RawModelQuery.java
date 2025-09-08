package org.minerift.ether.database.sql.op.dml;

import java.util.Arrays;

// A raw query belonging to a Model
public class RawModelQuery {

    private final String sql;
    private final String[] bindOrder;

    public RawModelQuery(String sql, String[] bindOrder) {
        this.sql = sql;
        this.bindOrder = bindOrder;
    }

    public RawModelQuery(String sql, String[]... bindOrderParts) {
        int i = 0, size = 0;
        for(String[] part : bindOrderParts) { size += part.length; }
        String[] bindOrder = new String[size];

        for(String[] part : bindOrderParts) {
            for(String field : part) {
                bindOrder[i++] = field;
            }
        }

        this.sql = sql;
        this.bindOrder = bindOrder;
    }

    public String getSql() {
        return sql;
    }

    public Object[] getNullValuesFromBindOrder() {
        return new Object[bindOrder.length];
    }

    public String[] getBindOrder() {
        return bindOrder;
    }

    @Override
    public String toString() {
        return "RawModelQuery{" +
                "sql='" + sql + '\'' +
                ", bindOrder=" + Arrays.toString(bindOrder) +
                '}';
    }
}
