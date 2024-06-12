package org.minerift.ether.database.sql;

import com.google.common.base.Preconditions;
import org.jooq.BatchBindStep;
import org.jooq.CloseableQuery;
import org.jooq.DataType;
import org.jooq.Field;
import org.minerift.ether.database.sql.fallback.Fallback;
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

    // TODO: refactor in favor of getPossibleFallback
    @Deprecated
    public static <T> boolean isDataTypeSupported(DataType<T> type, SQLDialect dialect) {
        Preconditions.checkNotNull(type);
        if(type.isArray() && dialect.getArraysFallback(type) != null) {
            return false;
        }
        return true;
    }

    public static <T> Fallback<T, ?> getPossibleFallback(DataType<T> type, SQLDialect dialect) {
        Preconditions.checkNotNull(type);
        if(type.isArray()) {
            return dialect.getArraysFallback(type);
        }
        /*else if (type.isUUID()) {
            return dialect.supportsUUIDs(type);
        }*/
        return null;
    }

    public static boolean testConnection(Connection conn, int timeout) {
        try {
            return conn.isValid(timeout);
        } catch (SQLException ex) {
            throw new IllegalArgumentException("Timeout cannot be negative!", ex);
        }
    }

    public static <M> BatchBindStep bindToBatch(BatchBindStep batch, Model<M, ?> model, Collection<M> objs, String[] bindOrder) {
        for(M obj : objs) {
            batch.bind(model.dumpBindValues(obj, bindOrder));
        }
        return batch;
    }

    @SafeVarargs
    public static String[] getBindOrder(Set<Field<?>> ... fields) {
        List<String> bindOrder = new ArrayList<>();
        for(Set<Field<?>> bindPart : fields) {
            for(Field<?> field : bindPart) {
                bindOrder.add(field.getName());
            }
        }
        System.out.println(bindOrder); // debug
        return bindOrder.toArray(String[]::new);
    }

    public static void bind(CloseableQuery query, Model<?, ?> model, NamedBindValues<?> bindVals, String[] bindOrder) {
        for(int i = 0; i < bindOrder.length; i++) {
            String column = bindOrder[i];
            Object javaVal = bindVals.getFieldValue(column);
            Object sqlVal = model.getField(column).readJavaAsSQLValue(javaVal); // fix: update java values to sql as appropriate
            //System.out.println("javaVal: " + javaVal); // debug
            //System.out.println("sqlVal: " + sqlVal); // debug
            query.bind(i + 1, sqlVal);
        }
    }

    // Binds an object's values to a parameterized query
    public static <M> void bind(CloseableQuery query, Model<M, ?> model, M obj, String[] bindOrder) {
        bind(query, model, model.dumpNamedBindValues_New(obj), bindOrder);
    }
}
