package org.minerift.ether.database.fallback;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.minerift.ether.database.DataType;

// SQL data type fallback for persisting T as a JSON string (VARCHAR)
// VARCHAR is the supported data type across all SQL dialects
// One of the main uses for this class is for array fallbacks (serialize as json if arrays are unsupported)
public class JsonFallback<T> extends Fallback<T, String> {

    private final static Gson gson = new GsonBuilder()
                                        .create();

    private final Class<T> typeClazz;

    public JsonFallback(Class<T> typeClazz) {
        super(DataType.VARCHAR(255)); // TODO: review size
        this.typeClazz = typeClazz;
    }

    // If string, just return the string itself, else transform into json
    @Override
    public String adaptTo(T obj) {
        return obj instanceof String str ? str : gson.toJson(obj);
    }

    @Override
    public T adaptFrom(String obj) {
        return gson.fromJson(obj, typeClazz);
    }
}
