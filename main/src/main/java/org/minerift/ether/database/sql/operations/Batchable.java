package org.minerift.ether.database.sql.operations;

import org.minerift.ether.database.sql.model.Model;
import org.minerift.ether.database.sql.operations.dml.DMLModelBatch;

public interface Batchable {

    <M> DMLModelBatch<M> getBatch(Model<M> model);

}
