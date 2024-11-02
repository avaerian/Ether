package org.minerift.ether.database;

import org.minerift.ether.database.nusql.NuSQLResult;

import java.util.HashMap;
import java.util.Map;

// Immutable by default, explicit Mutable
public class Record<MO> {

    protected Model<MO, ?> model;
    protected Map<Field<MO, ?, ?>, Object> values;
    protected int recordIdx;


    /**
     * TODO: result will need to be decoupled from jooq so that we can handle both binary/sql results
     * - binary results will hold a byte buffer that can be streamed row-by-row ?
     * - multiple different types of results can exist for different purposes
     *   - binary select results will hold a key-value map (if selecting from binary db or something similar/more manual)
     *   - binary select all results can hold a byte buffer for the file that its accessing, jumping to the appropriate rows based on b-trees (need to finalize binary db file format)
     *   - sql results can have the jooq backend (almost identical to current NuSQLResult)
     */

    public Record(NuSQLResult<MO> result, Map<Field<MO, ?, ?>, Object> values, int recordIdx) {
        this.model = result.getModel();
        this.values = values;
        this.recordIdx = recordIdx;
    }

    public <T> T readField() {
        throw new UnsupportedOperationException("Unimplemented");
    }

    public static class Mutable<MO> extends Record<MO> {

        public Mutable(NuSQLResult<MO> result, Map<Field<MO, ?, ?>, Object> values, int recordIdx) {
            super(result, values, recordIdx);
        }

        // Create a mutable record with values that we can set later
        public Mutable(NuSQLResult<MO> result, int recordIdx) {
            super(result, new HashMap<>(result.getModel().getFields().size()), recordIdx);
            model.getFields().forEach(field -> values.put(field, null));
        }

        public void setRecordIndex(int recordIdx) {
            this.recordIdx = recordIdx;
        }

        // val is either T or F (desired type or fallback type)
        // TODO: reconsider adding type safety back after data type decoupling
        public <T> void setFieldVal(Field<MO, T, ?> field, Object val) {
            values.put(field, val);
        }

        public void setFieldVals(Map<Field<MO, ?, ?>, Object> values) {
            this.values = values;
        }

        // sets from record index from result
        public void setFieldVals(NuSQLResult<MO> result) {
            values.forEach((field, oldVal) -> values.put(field, result.readField(field, recordIdx)));
        }

    }

}
