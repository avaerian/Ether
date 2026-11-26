package org.minerift.ether.database.nusql.query.cond;

import lombok.Getter;
import org.minerift.ether.database.Field;

import javax.annotation.Nullable;

@Getter
public class Eq<T> extends Condition {

    private final Field<?, T> field;
    @Nullable private final T value;

    public Eq(Field<?, T> field, @Nullable T value) {
        super();
        this.field = field;
        this.value = value;
    }

    @Override
    public CharSequence getSQLPart() {
        //StringBuilder str = new StringBuilder(field.getName()).append('=').append(value != null ? value : "?");
        return field.getName() + " = " + (value == null ? '?' : value);
    }

    @Override
    public Type type() {
        return Type.EQ;
    }
}
