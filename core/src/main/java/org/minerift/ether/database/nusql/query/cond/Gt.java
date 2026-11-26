package org.minerift.ether.database.nusql.query.cond;

import lombok.Getter;
import org.minerift.ether.database.Field;

import javax.annotation.Nullable;

@Getter
public class Gt<T extends Comparable<T>> extends Condition {
    private final Field<?, T> field;
    private final @Nullable T value;

    public Gt(Field<?, T> field, @Nullable T value) {
        this.field = field;
        this.value = value;
    }

    @Override
    public CharSequence getSQLPart() {
        return field.getName() + " > " + (value == null ? '?' : value);
    }

    @Override
    public Type type() {
        return Type.GT;
    }
}
