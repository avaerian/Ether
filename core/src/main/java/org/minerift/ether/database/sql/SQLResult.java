package org.minerift.ether.database.sql;

import org.jetbrains.annotations.NotNull;
import org.minerift.ether.database.Model;
import org.minerift.ether.database.Record;
import org.minerift.ether.database.Result;
import org.minerift.ether.util.fn.IBuilder;

import java.sql.ResultSet;
import java.util.Iterator;
import java.util.stream.Stream;

public class SQLResult<MO> extends Result<MO> {
    private final org.jooq.Result<org.jooq.Record> result;

    public SQLResult(Model<MO, ?> model, org.jooq.Result<org.jooq.Record> result) {
        super(model);
        this.result = result;
    }

    public org.jooq.Result<org.jooq.Record> asJooqResult() {
        return result;
    }

    public ResultSet asResultSet() {
        return result.intoResultSet();
    }

    @Override
    public int size() {
        return result.size();
    }

    @Override
    public SQLRecord<MO> getRecord(int idx) {
        return new SQLRecord<>(model, result.get(idx));
    }

    @Override
    public Stream<Record<MO>> stream() {
        return result.stream().map(record -> new SQLRecord<>(model, record));
    }

    @Override
    public Iterator<IBuilder<MO>> iterateBuilders() {
        return streamBuilders().iterator();
    }

    @NotNull
    @Override
    public Iterator<Record<MO>> iterator() {
        return stream().iterator();
    }
}
