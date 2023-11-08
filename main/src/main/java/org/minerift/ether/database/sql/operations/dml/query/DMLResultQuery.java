package org.minerift.ether.database.sql.operations.dml.query;

import org.jooq.CloseableResultQuery;
import org.jooq.Record;
import org.jooq.Result;
import org.minerift.ether.database.sql.model.Field;
import org.minerift.ether.database.sql.model.Model;

import java.util.List;

public class DMLResultQuery extends DMLQueryBase<DMLResultQuery, CloseableResultQuery<Record>> {
    public DMLResultQuery(CloseableResultQuery<Record> query, String[] bindOrder) {
        super(query, bindOrder);
    }

    public DMLResultQuery fetchSize(int fetchSize) {
        query.fetchSize(fetchSize);
        return this;
    }

    public DMLResultQuery maxRows(int maxRows) {
        query.maxRows(maxRows);
        return this;
    }

    public Result<Record> fetch() {
        return query.fetch();
    }

    public List<?> fetch(Field<?, ?, ?> field) {
        return query.fetch(field.getSQLField());
    }
}
