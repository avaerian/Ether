package org.minerift.ether.database.fallback;

import org.minerift.ether.database.DataType;

public class EnumOrdinalFallback<E extends Enum<E>> extends Fallback<E, Short> {

    private final Class<E> enumClazz;

    public EnumOrdinalFallback(DataType<E> type) {
        super(DataType.SHORT.nullable(type.isNullable()));
        this.enumClazz = type.getType();
    }

    @Override
    public Short adaptTo(E obj) {
        return (short) obj.ordinal();
    }

    @Override
    public E adaptFrom(Short obj) {
        return enumClazz.getEnumConstants()[obj.intValue()];
    }
}
