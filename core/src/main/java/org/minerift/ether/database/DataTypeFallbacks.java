package org.minerift.ether.database;

import org.minerift.ether.database.sql.fallback.Fallback;

public interface DataTypeFallbacks {
    <T> Fallback<T, ?> getPossibleFallback(DataType<T> type, DatabaseCreationContext ctx);
}
