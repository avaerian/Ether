package org.minerift.ether.database;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.minerift.ether.util.Utils.enumerateMap;

// MO -> Model Object
// T  -> Common data type (if any)
public class Fields<MO> implements Iterable<Field<MO, ?, ?>> {

    private final Model<MO, ?> model;
    private final Map<String, Field<MO, ?, ?>> fieldsByName;

    public Fields(Model<MO, ?> model, Map<String, Field<MO, ?, ?>> fieldsByName) {
        this.model = model;
        this.fieldsByName = fieldsByName;
    }

    /**
     * Get a new set of model fields, excluding requested fields
     * @param excludes fields to exclude
     * @return new Fields set without excluded fields
     */
    @SafeVarargs
    public final Fields<MO> exclude(Field<MO, ?, ?>... excludes) {
        Map<String, Field<MO, ?, ?>> newFields = new LinkedHashMap<>(fieldsByName);
        for(Field<MO, ?, ?> field : excludes) {
            newFields.remove(field.getName());
        }
        return new Fields<>(model, newFields);
    }

    public Field<MO, ?, ?> getField(String name) {
        return fieldsByName.get(name);
    }

    // NOTE: BE CAREFUL about using this method; not intended for modifying map
    public Map<String, Field<MO, ?, ?>> getFieldsByNameMap() {
        return fieldsByName;
    }

    public String[] getNames() {
        String[] names = new String[fieldsByName.size()];
        enumerateMap(fieldsByName, (i, field) -> names[i] = field.getKey());
        return names;
    }

    public int size() {
        return fieldsByName.size();
    }

    @NotNull
    @Override
    public Iterator<Field<MO, ?, ?>> iterator() {
        return fieldsByName.values().iterator();
    }
}
