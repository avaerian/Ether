package org.minerift.ether.database.sql.model;

import org.jetbrains.annotations.NotNull;
import org.minerift.ether.util.reflect.Reflect;
import org.minerift.ether.util.reflect.ReflectedObject;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.function.Predicate;

public class Fields<M, T> implements Iterable<Field<M, T, ?>> {

    private EnumSet<Flag> flags;
    private final Model<M, ?> model;
    private final Field<M, T, ?>[] fields;

    public static <M> Fields<M, ?> of(Model<M, ?> model, Field<M, ?, ?>[] fields) {
        return new Fields<>(model, (Field<M, Object, ?>[]) fields);
    }

    public Fields(Model<M, ?> model, Field<M, T, ?>[] fields) {
        this(model, fields, EnumSet.noneOf(Flag.class));
    }

    private Fields(Model<M, ?> model, Field<M, T, ?>[] fields, EnumSet<Flag> flags) {
        this.model = model;
        this.fields = fields;
        this.flags = flags;

        // TODO: refactor
        if(isOnlyPrimaryKeysTest()) {
            flags.add(Flag.ONLY_PRIMARY_KEYS);
        }

        if(isOnlyUniqueKeysTest()) {
            flags.add(Flag.ONLY_UNIQUE_FIELDS);
        }
    }

    public String[] getNames() {
        return Arrays.stream(fields).map(field -> field.asJooqField().getName()).toArray(String[]::new);
    }

    @Deprecated
    public Fields<M, T> getPrimaryKeys() {
        if(flags.contains(Flag.ONLY_PRIMARY_KEYS)) {
            return this;
        } else {
            ReflectedObject<Model<M, ?>> reflectedModel = Reflect.of(model);
            Field<M, T, ?>[] primaryKeys = reflectedModel.getFieldsFromRefs(fields)
                    .filter(field -> field.hasAnnotation(PrimaryKey.class))
                    .readAllTyped(model, Field.class);
            return new Fields<>(model, primaryKeys, EnumSet.of(Flag.ONLY_PRIMARY_KEYS));
        }
    }

    @Deprecated
    public Field<M, T, ?> getPrimaryKey() {
        ReflectedObject<Model<M, ?>> reflectedModel = Reflect.of(model);
        Field<M, T, ?>[] primaryKeys = reflectedModel.getFieldsFromRefs(fields)
                .filter(field -> field.hasAnnotation(PrimaryKey.class))
                .readAllTyped(model, Field.class);

        if(primaryKeys.length != 1) {
            throw new IllegalStateException("There can only be 1 primary key in a model!");
        }
        return primaryKeys[0];
    }

    public Fields<M, T> getUniqueFields() {
        if(isOnlyUniqueFields()) {
            return this;
        } else {
            ReflectedObject<Model<M, ?>> reflectedModel = Reflect.of(model);
            Field<M, T, ?>[] primaryKeys = reflectedModel.getFieldsFromRefs(fields)
                    .filter(field -> field.hasAnnotation(Unique.class))
                    .readAllTyped(model, Field.class);
            return new Fields<>(model, primaryKeys, EnumSet.of(Flag.ONLY_UNIQUE_FIELDS));
        }
    }

    public org.jooq.Field<?>[] asSQLFields() {
        org.jooq.Field<?>[] sqlFields = new org.jooq.Field[fields.length];
        for (int i = 0; i < fields.length; i++) {
            sqlFields[i] = fields[i].asJooqField();
        }
        return sqlFields;
    }

    public boolean isEmpty() {
        return fields.length == 0;
    }

    public int size() {
        return fields.length;
    }

    public boolean isOnlyPrimaryKeys() {
        return flags.contains(Flag.ONLY_PRIMARY_KEYS);
    }

    public boolean isOnlyUniqueFields() {
        return flags.contains(Flag.ONLY_UNIQUE_FIELDS);
    }

    private boolean isOnlyPrimaryKeysTest() {
        final ReflectedObject<Model<M, ?>> reflectedModel = Reflect.of(model);
        return reflectedModel.getFieldsFromRefs(fields).hasAnnotation(PrimaryKey.class);
        //return all(field -> reflectedModel.getFieldFromRef(field).hasAnnotation(PrimaryKey.class));
    }

    private boolean isOnlyUniqueKeysTest() {
        final ReflectedObject<Model<M, ?>> reflectedModel = Reflect.of(model);
        return reflectedModel.getFieldsFromRefs(fields).hasAnnotation(Unique.class);
        //return all(field -> reflectedModel.getFieldFromRef(field).hasAnnotation(Unique.class));
    }

    public Field<M, ?, ?>[] getArray() {
        return fields;
    }

    @NotNull
    @Override
    public Iterator<Field<M, T, ?>> iterator() {
        return Arrays.stream(fields).iterator();
    }

    public Fields<M, T> filter(Predicate<Field<M, T, ?>> predicate) {
        return new Fields<>(model, Arrays.stream(fields).filter(predicate).toArray(Field[]::new));
    }

    public boolean all(Predicate<Field<M, ?, ?>> condition) {
        for(Field<M, ?, ?> field : fields) {
            if(!condition.test(field)) {
                return false;
            }
        }
        return true;
    }

    private enum Flag {
        ONLY_PRIMARY_KEYS,
        ONLY_UNIQUE_FIELDS
    }
}
