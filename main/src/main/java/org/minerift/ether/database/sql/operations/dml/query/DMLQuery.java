package org.minerift.ether.database.sql.operations.dml.query;

import org.jooq.CloseableQuery;

public class DMLQuery extends DMLQueryBase<DMLQuery, CloseableQuery> {
    public DMLQuery(CloseableQuery query, String[] bindOrder) {
        super(query, bindOrder);
    }
}
