package org.minerift.ether.database.sql.operations.dml.cache;

import org.jooq.Field;
import org.minerift.ether.database.sql.SQLUtils;

import java.util.Set;

public final class RawBindQuery {

    private final String sql;
    private final String[] bindOrder;

    public RawBindQuery(String sql, String[] bindOrder) {
        this.sql = sql;
        this.bindOrder = bindOrder;
    }

    public RawBindQuery(String sql, Set<Field<?>>... bindFieldNames) {
        this(sql, SQLUtils.getBindOrder(bindFieldNames));
    }

    public String getSQL() {
        return sql;
    }

    public String[] getBindOrder() {
        return bindOrder;
    }
}
