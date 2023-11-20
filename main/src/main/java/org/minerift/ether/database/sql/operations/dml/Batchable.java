package org.minerift.ether.database.sql.operations.dml;

import org.minerift.ether.database.sql.model.Model;

public interface Batchable {

    <M> DMLModelBatch<M> batchFor(Model<M> model);

}
