package org.minerift.ether.util.reflect;

import com.google.common.collect.BiMap;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Represents a reflected class that has been mapped for an object.
// This means that we can store/access values for fields easily,
// as well as access attributes with a less verbose and more organized API.
public class ReflectedObject<T> {

    private final T holder;
    private final ReflectedClass<T> reflectedClass;

    public ReflectedObject(T holder) {
        this.holder = holder;
        this.reflectedClass = new ReflectedClass<>((Class<T>) holder.getClass());
    }

    public ReflectedField getField(String name) throws NoSuchFieldException {
        return reflectedClass.getField(name);
    }

    public ReflectedField getFieldOrNull(String name) {
        return reflectedClass.getFieldOrNull(name);
    }

    public ReflectedFields getPublicFields() {
        return reflectedClass.getPublicFields();
    }

    public ReflectedFields getFields() {
        return reflectedClass.getFields();
    }

    // TODO: NOT WORKING
    @Deprecated
    public ReflectedField getFieldFromRef(Object fieldVal) {
        Map<ReflectedField, Object> fields2Vals = reflectedClass.mapFieldsToValues(holder);
        for(var entry : fields2Vals.entrySet()) {
            if(entry.getValue().equals(fieldVal)) {
                return entry.getKey();
            }
        }
        throw new IllegalArgumentException("Field couldn't be found for value " + fieldVal);
    }

    // TODO: NOT WORKING
    public ReflectedFields getFieldsFromRefs(Object[] fieldVals) {
        Map<ReflectedField, Object> fields2Vals = reflectedClass.mapFieldsToValues(holder);
        final ReflectedField[] fields = new ReflectedField[fieldVals.length];
        for(int i = 0; i < fields.length; i++) {
            for(var entry : fields2Vals.entrySet()) {
                if(entry.getValue() == fieldVals[i]) {
                    fields[i] = entry.getKey();
                    break;
                }
            }

            if(fields[i] == null) {
                throw new IllegalArgumentException("Field couldn't be found for value " + fieldVals[i]);
            }
        }
        return new ReflectedFields(fields);
    }

    public <V> V readField(ReflectedField field) {
        return (V) field.getValue(holder);
    }

    public <V> V readField(String fieldName) {
        try {
            return readField(getField(fieldName));
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public <V> V readField(String fieldName, Class<V> clazz) {
        return readField(fieldName);
    }

    public <V> List<V> readTypedFields(ReflectedField ... fields) {
        return Arrays.stream(fields).map(field -> (V)readField(field)).toList();
    }

    public List<Object> readFields(ReflectedField ... fields) {
        return Arrays.stream(fields).map(this::readField).toList();
    }

}
