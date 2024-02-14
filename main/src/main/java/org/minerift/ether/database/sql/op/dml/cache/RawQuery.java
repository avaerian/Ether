package org.minerift.ether.database.sql.op.dml.cache;

import org.jooq.Field;
import org.minerift.ether.database.sql.SQLUtils;

import java.util.Set;

public class RawQuery {

    private final String sql;
    private final String[] bindOrder;

    public RawQuery(String sql, String[] bindOrder) {
        this.sql = sql;
        this.bindOrder = bindOrder;
    }

    public RawQuery(String sql, Set<Field<?>>... bindFieldNames) {
        this(sql, SQLUtils.getBindOrder(bindFieldNames));
    }

    public String getSql() {
        return sql;
    }

    public Object[] getEmptyBindOrder() {
        return new Object[bindOrder.length];
    }

    public String[] getBindOrder() {
        return bindOrder;
    }
}
