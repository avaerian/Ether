package org.minerift.ether.util.reflect;

import com.google.common.collect.BiMap;

import java.util.Arrays;
import java.util.List;

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

    public ReflectedFields getPublicFields() {
        return reflectedClass.getPublicFields();
    }

    public ReflectedFields getFields() {
        return reflectedClass.getFields();
    }

    public ReflectedField getFieldFromRef(Object fieldVal) {
        return reflectedClass.mapFieldsToValues(holder).inverse().get(fieldVal);
    }

    public ReflectedField[] getFieldsFromRefs(Object ... fieldVals) {
        final BiMap<Object, ReflectedField> valuesToFields = reflectedClass.mapFieldsToValues(holder).inverse();
        final ReflectedField[] fields = new ReflectedField[fieldVals.length];
        for(int i = 0; i < fields.length; i++) {
            fields[i] = valuesToFields.get(fieldVals[i]);
        }
        return fields;
    }

    public <V> V readField(ReflectedField field) {
        return (V) field.get(this);
    }

    public <V> List<V> readTypedFields(ReflectedField ... fields) {
        return Arrays.stream(fields).map(field -> (V)readField(field)).toList();
    }

    public List<Object> readFields(ReflectedField ... fields) {
        return Arrays.stream(fields).map(this::readField).toList();
    }

}
