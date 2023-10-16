package org.minerift.ether.database.sql.model;

import org.jetbrains.annotations.NotNull;
import org.minerift.ether.util.reflect.Reflect;
import org.minerift.ether.util.reflect.ReflectedObject;

import java.util.Arrays;
import java.util.Iterator;

public class Fields<M, T> implements Iterable<Field<M, T>> {

    private boolean isOnlyPrimaryKeys;
    private final Model<M> model;
    private final Field<M, T>[] fields;

    public static <M> Fields<M, ?> of(Model<M> model, Field<M, ?>[] fields) {
        return new Fields<>(model, (Field<M, Object>[]) fields);
    }

    public Fields(Model<M> model, Field<M, T>[] fields) {
        this.model = model;
        this.fields = fields;
        this.isOnlyPrimaryKeys = Arrays.equals(getPrimaryKeys().fields, fields);
    }

    private Fields(Model<M> model, Field<M, T>[] fields, boolean isOnlyPrimaryKeys) {
        this.model = model;
        this.fields = fields;
        this.isOnlyPrimaryKeys = isOnlyPrimaryKeys;
    }

    public Fields<M, T> getPrimaryKeys() {
        if(isOnlyPrimaryKeys) {
            return this;
        } else {
            ReflectedObject<Model<M>> reflectedModel = Reflect.of(model);
            Field<M, T>[] primaryKeys = Arrays.stream(reflectedModel.getFieldsFromRefs(fields))
                    .filter(field -> field.hasAnnotation(PrimaryKey.class))
                    .map(field -> (Field)reflectedModel.readField(field))
                    .toArray(Field[]::new);
            return new Fields<>(model, primaryKeys, true);
        }
    }

    public org.jooq.Field<T>[] asSQLFields() {
        org.jooq.Field<T>[] sqlFields = new org.jooq.Field[fields.length];
        for (int i = 0; i < fields.length; i++) {
            sqlFields[i] = fields[i].getSQLField();
        }
        return sqlFields;
    }

    public boolean isOnlyPrimaryKeys() {
        return isOnlyPrimaryKeys;
    }

    public Field<M, ?>[] getArray() {
        return fields;
    }

    @NotNull
    @Override
    public Iterator<Field<M, T>> iterator() {
        return Arrays.stream(fields).iterator();
    }
}
