package org.minerift.ether.database.nusql.op.ddl;

import org.jooq.Constraint;
import org.jooq.DSLContext;
import org.minerift.ether.database.DataType;
import org.minerift.ether.database.Field;
import org.minerift.ether.database.Model;
import org.minerift.ether.database.nusql.NuSQLAccess;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.jooq.impl.DSL.*;
import static org.minerift.ether.database.nusql.SQLUtils.*;

public class DDLCreateTable {

    public static <MO, PK> void createTableFromModel(NuSQLAccess access, Model<MO, PK> model) {

        List<Constraint> constraints = new ArrayList<>();

        for(Field<MO, ?, ?> field : model.getFields()) {
            // Handle foreign field constraints
            Field<?, ?, ?> parentField = model.getForeignFields().getParentField(field);
            if(parentField != null) {
                Model<?, ?> parentModel = access.db().getModel(parentField.getOwner());
                constraints.add(foreignKey(field.getName()).references(name(parentModel.getTableName()), name(parentField.getName())));
            }


            // Handle enum constraints
            // TODO: DDL op for updating enum constraints for a column
            DataType<?> type = field.getRequestedDataType();
            if(type.isEnumType()) {
                var constraint = constraint(field.getName() + "_enum");
                Enum<?>[] enumVals = (Enum<?>[]) type.getType().getEnumConstants();
                constraints.add(
                        switch(type.getPrimitiveType()) {
                            case ENUM_ORDINAL -> constraint.check(asJooqField(field).in(Stream.of(enumVals).map(Enum::ordinal).toList()));
                            case ENUM_STR -> constraint.check(asJooqField(field).in(Stream.of(enumVals).map(Enum::name).toList()));
                            default -> throw new IllegalStateException("Unexpected value: " + type.getPrimitiveType());
                        }
                );
            }
        }

        final DSLContext dsl = access.dsl();
        dsl.createTable(asJooqTable(model))
                .columns(asJooqFields(model.getFields()))
                .primaryKey(asJooqField(model.getPrimaryKey()))
                .constraints(constraints)
                .execute();
    }

}
