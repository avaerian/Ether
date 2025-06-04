package org.minerift.ether.database.nusql;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import org.jooq.BatchBindStep;
import org.jooq.Query;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.impl.DefaultDataType;
import org.jooq.impl.SQLDataType;
import org.minerift.ether.database.*;
import org.minerift.ether.database.nusql.fallback.EnumOrdinalFallback;
import org.minerift.ether.database.nusql.fallback.EnumStrFallback;
import org.minerift.ether.database.nusql.fallback.NuFallback;
import org.minerift.ether.database.nusql.op.bind.NamedBindValues;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;
import static org.minerift.ether.util.Utils.enumerateMap;

public class SQLUtils {

    public static final String[] EMPTY_BIND_VALS = new String[0];

    public static <T> NuFallback<T, ?> getPossibleFallback(DataType<T> type, DatabaseCreationContext dbCtx) {
        if(dbCtx instanceof SQLDatabaseCreationContext sqlCtx) {
            return getPossibleFallback(type, sqlCtx.dialect());
        }
        return null; // only two types of databases at the moment, so safe to assume if database creation ctx is binary then no fallbacks
    }

    @Beta
    public static <T> NuFallback<T, ?> getPossibleFallback(DataType<T> type, SQLDialect dialect) {
        Preconditions.checkNotNull(type);
        if(type.isArrayType()) {
            return dialect.getArraysFallback(type);
        }
        if(type.isEnumType()) {
            return switch(type.getPrimitiveType()) {
                case ENUM_ORDINAL -> new EnumOrdinalFallback<>((DataType<? extends Enum>)type);
                case ENUM_STR -> new EnumStrFallback<>((DataType<? extends Enum>)type);
                default -> throw new IllegalStateException("Unexpected value: " + type.getPrimitiveType());
            };
        }
        /*else if (type.isUUID()) {
            return dialect.supportsUUIDs(type);
        }*/
        return null;
    }

    public static <T> org.jooq.DataType<T> asJooqDataType(DataType<T> type, org.jooq.SQLDialect dialect) {
        org.jooq.DataType<?> jooqType = DefaultDataType.getDataType(dialect, type.getTypeName());

        // Debug
        //System.out.printf("jooq type name: %s, native type name: %s\n", jooqType.getTypeName(), type.getTypeName());

        jooqType = jooqType.nullable(type.isNullable());
        if(type.isArrayType()) jooqType = jooqType.array();
        if(type.hasLength()) jooqType = jooqType.length(type.length());

        return (org.jooq.DataType<T>) jooqType;
    }

    public static <T> org.jooq.DataType<T> asJooqDataType(DataType<T> type, SQLDialect dialect) {
        return asJooqDataType(type, dialect.asJooqDialect());
    }

    public static <T> org.jooq.DataType<T> asJooqDataType(DataType<T> type) {
        return asJooqDataType(type, org.jooq.SQLDialect.DEFAULT);
    }

    public static org.jooq.Field<?> asJooqField(Field<?, ?, ?> field) {
        return field(field.getName(), asJooqDataType(field.getDataType()));
    }

    public static org.jooq.Field<?>[] asJooqFields(Fields<?> fields) {
        org.jooq.Field<?>[] jooqFields = new org.jooq.Field[fields.size()];
        enumerateMap(fields.getFieldsByNameMap(), (i, field) -> jooqFields[i] = asJooqField(field.getValue()));
        return jooqFields;
    }

    public static Table<Record> asJooqTable(Model<?, ?> model) {
        return asJooqTable(model.getTableName());
    }

    public static Table<Record> asJooqTable(String tableName) {
        return table(tableName);
    }

    public static Map<org.jooq.Field<?>, ?> getNullBindValue(Field<?, ?, ?> field) {
        return getNullBindValue(asJooqField(field));
    }

    public static Map<org.jooq.Field<?>, ?> getNullBindValue(org.jooq.Field<?> field) {
        return Collections.singletonMap(field, null);
    }

    public static Map<org.jooq.Field<?>, ?> getNullBindValues(Fields<?> fields) {
        Map<org.jooq.Field<?>, ?> bindVals = new LinkedHashMap<>(fields.size());
        fields.forEach((field) -> bindVals.put(asJooqField(field), null));
        return bindVals;
    }

    public static boolean testConnection(Connection conn, int timeout) {
        try {
            return conn.isValid(timeout);
        } catch (SQLException ex) {
            throw new IllegalArgumentException("Timeout cannot be negative!", ex);
        }
    }

    public static <M> void bind(BatchBindStep batch, Model<M, ?> model, Collection<M> objs, String[] bindOrder) {
        for(M obj : objs) {
            batch.bind(model.dumpOrderedBindValues(obj, bindOrder));
        }
    }

    @Deprecated
    @SafeVarargs
    public static String[] getBindOrder(Set<org.jooq.Field<?>> ... fields) {
        List<String> bindOrder = new ArrayList<>();
        for(Set<org.jooq.Field<?>> bindPart : fields) {
            for(org.jooq.Field<?> field : bindPart) {
                bindOrder.add(field.getName());
            }
        }
        System.out.println(bindOrder); // debug
        return bindOrder.toArray(String[]::new);
    }

    public static void bind(Query query, Model<?, ?> model, NamedBindValues<?> bindVals, String[] bindOrder) {
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
    public static <M> void bind(Query query, Model<M, ?> model, M obj, String[] bindOrder) {
        bind(query, model, model.dumpNamedBindValues(obj), bindOrder);
    }
}
