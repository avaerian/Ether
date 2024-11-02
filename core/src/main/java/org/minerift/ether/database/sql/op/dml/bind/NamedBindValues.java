package org.minerift.ether.database.sql.op.dml.bind;

import com.google.common.base.Preconditions;
import org.minerift.ether.database.Field;
import org.minerift.ether.database.Fields;
import org.minerift.ether.database.Model;

import java.util.HashMap;
import java.util.Map;

public interface NamedBindValues<T> {

    @Deprecated
    static SingleNamedBindValue<?> nulled(org.minerift.ether.database.sql.model.Field<?, ?, ?> field) {
        return new SingleNamedBindValue<>(field.getName(), null);
    }

    @Deprecated
    static ManyNamedBindValues<?> nulled(org.minerift.ether.database.sql.model.Fields<?, ?> fields) {
        Map<String, Object> namedBindVals = new HashMap<>();
        fields.forEach(field -> namedBindVals.put(field.getName(), null));
        return new ManyNamedBindValues<>(namedBindVals);
    }

    @Deprecated
    static <T> SingleNamedBindValue<T> of(org.minerift.ether.database.sql.model.Field<?, T, ?> field, T bindVal) {
        return of(field.getName(), bindVal);
    }

    static SingleNamedBindValue<?> nulled(Field<?, ?, ?> field) {
        return new SingleNamedBindValue<>(field.getName(), null);
    }

    static ManyNamedBindValues<?> nulled(Fields<?> fields) {
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

    T getFieldValue(String field);

    Map<String, T> asMap();

    @Deprecated
    default Object[] getValuesFromBindOrder(org.minerift.ether.database.sql.model.Model<?, ?> model, String[] bindOrder) {
        Object[] objs = new Object[bindOrder.length];
        for(int i = 0; i < bindOrder.length; i++) {
            String column = bindOrder[i];
            objs[i] = model.getField(column).readJavaAsSQLValue(getFieldValue(bindOrder[i]));
        }
        return objs;
    }

    default Object[] getValuesFromBindOrder(Model<?, ?> model, String[] bindOrder) {
        Object[] objs = new Object[bindOrder.length];
        for(int i = 0; i < bindOrder.length; i++) {
            String column = bindOrder[i];
            objs[i] = model.getField(column).readJavaAsSQLValue(getFieldValue(bindOrder[i]));
        }
        return objs;
    }
}
