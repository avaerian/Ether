package org.minerift.ether.database.sql.fallback;

import com.google.gson.Gson;
import org.jooq.impl.SQLDataType;

public class JsonFallback<T> extends Fallback<T, String> {

    private final static Gson gson = new Gson();

    private final Class<T> typeClazz;

    public JsonFallback(Class<T> typeClazz) {
        super(SQLDataType.VARCHAR(255));
        this.typeClazz = typeClazz;
    }

    @Override
    public String adaptTo(Object obj) {
        return gson.toJson(obj);
    }

    @Override
    public T adaptFrom(String obj) {
        return gson.fromJson(obj, typeClazz);
    }
}
