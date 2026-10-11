package org.minerift.ether.database.sql;

import org.minerift.ether.database.DataType;
import org.minerift.ether.database.DataTypeFallbacks;
import org.minerift.ether.database.DatabaseCreationContext;
import org.minerift.ether.database.fallback.Fallback;

public class SqlDataTypeFallbacks implements DataTypeFallbacks {
    @Override
    public <T> Fallback<T, ?> getPossibleFallback(DataType<T> type, DatabaseCreationContext ctx) {
        return null;
    }
}
