package org.minerift.ether.database.sql.fallback;

import org.minerift.ether.database.DataType;
import org.minerift.ether.util.Default;
import org.minerift.ether.util.reflect.Reflect;
import org.minerift.ether.util.reflect.ReflectedField;
import org.minerift.ether.util.reflect.ReflectedFields;

public class EnumStrFallback<E extends Enum<E>> extends Fallback<E, String> {

    // TODO: add cache so that multiple columns that have the same enum type use the same fallback (review)

    private final Class<E> enumClazz;

    public EnumStrFallback(DataType<E> type) {
        super(DataType.VARCHAR(type.length()).nullable(type.isNullable()));
        this.enumClazz = type.getType();
    }

    @Override
    public String adaptTo(E obj) {
        return obj.name();
    }

    /**
     * Adapts a stringified enum to the appropriate enum value.
     * If the string is not a valid enum, return the first enum
     * annotated as {@link org.minerift.ether.util.Default},
     * otherwise throw exception.
     *
     * @param obj stringified enum value
     * @return adapted enum value
     * @throws IllegalArgumentException if string isn't valid and no default is found
     */
    @Override
    public E adaptFrom(String obj) throws IllegalArgumentException {
        try {
            return Enum.valueOf(enumClazz, obj.toUpperCase());
        } catch (IllegalArgumentException ex) {
            ReflectedFields fields = Reflect.of(enumClazz).getFields();
            for(ReflectedField field : fields) {
                Default def = field.getAnnotation(Default.class);
                if(def != null) {
                    return Enum.valueOf(enumClazz, field.getName());
                }
            }
            throw ex;
        }
    }
}
