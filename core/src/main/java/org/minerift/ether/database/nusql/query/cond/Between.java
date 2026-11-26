package org.minerift.ether.database.nusql.query.cond;

import lombok.Getter;
import org.minerift.ether.database.Field;

import javax.annotation.Nullable;

public class Between<T extends Comparable<T>> extends Condition {

    @Getter private final Field<?, T> field;
    @Nullable private final T lo, hi;

    public Between(Field<?, T> field) {
        this(field, null, null);
    }

    public Between(Field<?, T> field, @Nullable T lo, @Nullable T hi) {
        this.field = field;
        this.lo = lo;
        this.hi = hi;
    }

    @Override
    public CharSequence getSQLPart() {
        return field.getName() + " BETWEEN " +
                (lo == null ? '?' : lo) + " AND " +
                (hi == null ? '?' : hi);
    }

    @Override
    public Type type() {
        return Type.BETWEEN;
    }
}
