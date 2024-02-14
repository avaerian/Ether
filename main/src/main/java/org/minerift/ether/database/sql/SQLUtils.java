package org.minerift.ether.database.sql;

import com.google.common.base.Preconditions;
import org.jooq.BatchBindStep;
import org.jooq.CloseableQuery;
import org.jooq.DataType;
import org.jooq.Field;
import org.minerift.ether.database.sql.model.Model;
import org.minerift.ether.database.sql.op.dml.bind.NamedBindValues;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class SQLUtils {

    public static final String[] EMPTY_BIND_VALS = new String[0];

    // TODO: add more checks
    public static <T> boolean isDataTypeSupported(DataType<T> type, SQLDialect dialect) {
        Preconditions.checkNotNull(type);
        if(type.isArray() && !dialect.supportsArrays()) {
            return false;
        }
        return true;
    }

    public static boolean testConnection(Connection conn, int timeout) {
        try {
            return conn.isValid(timeout);
        } catch (SQLException ex) {
            throw new IllegalArgumentException("Timeout cannot be negative!", ex);
        }
    }

    public static <M> BatchBindStep bindToBatch(BatchBindStep batch, Model<M, ?> model, Collection<M> objs) {
        for(M obj : objs) {
            batch.bind(model.dumpBindValues(obj));
        }
        return batch;
    }

    public static String[] getBindOrder(Set<Field<?>> ... fields) {
        List<String> bindOrder = new ArrayList<>();
        for(Set<Field<?>> bindPart : fields) {
            for(Field<?> field : bindPart) {
                bindOrder.add(field.getName());
            }
        }
        System.out.println(bindOrder);
        return bindOrder.toArray(String[]::new);
    }


    public static void bind(CloseableQuery query, NamedBindValues<?> bindVals, String[] bindOrder) {
        for(int i = 0; i < bindOrder.length; i++) {
            query.bind(i + 1, bindVals.getField(bindOrder[i]));
        }
    }

    // Binds an object's values to a parameterized query
    public static <M> void bind(CloseableQuery query, Model<M, ?> model, M obj, String[] bindOrder) {
        bind(query, model.dumpNamedBindValues_New(obj), bindOrder);
    }
}
