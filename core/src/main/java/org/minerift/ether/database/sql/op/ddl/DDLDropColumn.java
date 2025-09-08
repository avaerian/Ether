package org.minerift.ether.database.sql.op.ddl;

import org.minerift.ether.database.DatabaseException;
import org.minerift.ether.database.Field;
import org.minerift.ether.database.Model;
import org.minerift.ether.database.sql.SQLAccess;

import java.util.List;

import static org.minerift.ether.database.sql.SQLUtils.asJooqField;
import static org.minerift.ether.database.sql.SQLUtils.asJooqTable;

public class DDLDropColumn {

    // TODO: review whether ALTER statement committing needs to be handled
    public static void dropColumnFromField(SQLAccess access, Field<?, ?, ?> field, boolean dropChildren) { // TODO: refactor "dropChildren" to "cascade" ?
        // - Check field for any fields that depend on this as a foreign key
        // IGNORE: Check if field is enum and needs enum constraint removed

        Model<?, ?> model = access.db().getModel(field.getOwner());
        List<Field<?,?,?>> children = ((Model)model).getForeignFields().getChildrenFields(field);

        if(!children.isEmpty()) {
            if(dropChildren) {

                for(var childField : children) {
                    Model childModel = access.db().getModel(childField.getOwner());
                    access.dsl().alterTable(asJooqTable(childModel))
                            .dropColumn(asJooqField(childField))
                            .execute();
                }

            } else {
                throw new DatabaseException("There are existing fields that depend on the field to be dropped, so field can't be dropped.");
            }
        }

        access.dsl().alterTable(asJooqTable(model))
                .dropColumn(asJooqField(field))
                .execute();
    }

}
