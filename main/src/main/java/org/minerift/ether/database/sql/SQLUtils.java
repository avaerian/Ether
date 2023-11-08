package org.minerift.ether.database.sql;

import com.google.common.base.Preconditions;
import org.jooq.DataType;
import org.jooq.Field;

import java.util.ArrayList;
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
}
