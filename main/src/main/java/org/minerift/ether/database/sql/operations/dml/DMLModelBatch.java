package org.minerift.ether.database.sql.operations.dml;

import org.jooq.BatchBindStep;
import org.jooq.CloseableQuery;
import org.minerift.ether.database.sql.SQLContext;
import org.minerift.ether.database.sql.model.Model;

// Represents a batched DML operation with model-based bind values
public class DMLModelBatch<M> {

    private final Model<M> model;
    private final BatchBindStep batch;
    private final String[] bindOrder;

    public DMLModelBatch(SQLContext ctx, Model<M> model, CloseableQuery query, String[] bindOrder) {
        this.model = model;
        this.batch = ctx.dsl().batch(query);
        this.bindOrder = bindOrder;
    }

    public DMLModelBatch<M> bind(M obj) {
        batch.bind(model.dumpBindValues(obj, bindOrder));
        return this;
    }

    public DMLModelBatch<M> bindAll(Iterable<M> objs) {
        objs.forEach(obj -> batch.bind(model.dumpBindValues(obj, bindOrder)));
        return this;
    }

    public int[] execute() {
        return batch.execute();
    }
}
