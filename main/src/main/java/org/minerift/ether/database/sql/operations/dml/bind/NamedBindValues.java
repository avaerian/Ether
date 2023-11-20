package org.minerift.ether.database.sql.operations.dml.bind;

import com.google.common.base.Preconditions;
import org.minerift.ether.database.sql.model.Field;
import org.minerift.ether.database.sql.model.Fields;

import java.util.HashMap;
import java.util.Map;

public interface NamedBindValues<T> {

    static SingleNamedBindValue<?> empty(Field<?, ?, ?> field) {
        return new SingleNamedBindValue<>(field.getName(), null);
    }

    static ManyNamedBindValues<?> empty(Fields<?, ?> fields) {
        Map<String, Object> namedBindVals = new HashMap<>();
        fields.forEach(field -> namedBindVals.put(field.getName(), null));
        return new ManyNamedBindValues<>(namedBindVals);
    }

    static <T> SingleNamedBindValue<T> of(String field, T bindVal) {
        return new SingleNamedBindValue<>(field, bindVal);
    }

    static <T> SingleNamedBindValue<T> of(Field<?, T, ?> field, T bindVal) {
        return of(field.getName(), bindVal);
    }

    static ManyNamedBindValues<?> of(String[] fields, Object[] bindVals) {
        Preconditions.checkArgument(fields.length == bindVals.length);
        Map<String, Object> namedBindVals = new HashMap<>(fields.length);
        for(int i = 0; i < fields.length; i++) {
            namedBindVals.put(fields[i], bindVals[i]);
        }
        return of(namedBindVals);
    }

    static <T> ManyNamedBindValues<T> of(Map<String, T> namedBindVals) {
        return new ManyNamedBindValues<>(namedBindVals);
    }

    T getField(String field);
}
