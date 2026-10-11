package org.minerift.ether.database;

import org.minerift.ether.database.fallback.Fallback;

public interface DataTypeFallbacks {
    <T> Fallback<T, ?> getPossibleFallback(DataType<T> type, DatabaseCreationContext ctx);
}
