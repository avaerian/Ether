package org.minerift.ether.database;

import com.google.common.collect.HashBiMap;
import org.minerift.ether.util.Utils;

import java.util.*;

// Provides access to the relationships between foreign fields and dependents for a model
public class ForeignFields<MO> {

    /**
     *
     * Re-evaluate what is needed for Foreign Fields:
     * - For a model's field, get a list of fields from external models depending on this one
     * - For a external field, get the field from this model that external field depends on
     * 
     *
     */




    private static final int INIT_LOOKUP_CAPACITY = 4; // not expecting many foreign fields

    private final Model<MO, ?> parentModel;

    private Map<Field<MO, ?, ?>, List<Field<?, ?, ?>>> parentToChildrenLookup;
    private Map<Field<MO, ?, ?>, Field<?, ?, ?>> childToParentLookup;

    private Set<Model<?, ?>> dependingModels; // models that depend on this model
    private Set<Model<?, ?>> parentModels; // models that this model depends on

    public ForeignFields(Model<MO, ?> model) {
        this.parentModel = model;
        this.parentToChildrenLookup = Collections.emptyMap();
        this.childToParentLookup = Collections.emptyMap();
        this.dependingModels = Collections.emptySet();
        this.parentModels = Collections.emptySet();
    }

    public <DMO> void addDependent(Field<MO, ?, ?> parentField, Model<DMO, ?> dependentModel, Field<DMO, ?, ?> dependentField) {
        System.out.println(this.parentModel.getPrimaryKey());
        System.out.println(parentModel.getPrimaryKey());
        System.out.println(dependentField);
        System.out.println(parentField);
        if (this.parentModel.getPrimaryKey() != parentField) {
            throw new IllegalArgumentException(String.format("%s from model %s is not a primary key", parentField.getName(), parentField.getOwner()));
        }

        initializeIfNeeded();
        dependentModel.foreignFields.initializeIfNeeded();

        parentToChildrenLookup.computeIfAbsent(parentField, (ignore) -> new ArrayList<>(INIT_LOOKUP_CAPACITY))
                .add(dependentField);

        dependentModel.foreignFields.childToParentLookup.put(dependentField, parentField);

        dependingModels.add(dependentModel);
        dependentModel.foreignFields.parentModels.add(parentModel);
    }

    // NOTE: no need for dropRelationship() because these relationships should never be need to be updated after fields are instantiated

    public List<Field<?, ?, ?>> getChildrenFields(Field<MO, ?, ?> parentField) {
        var result = parentToChildrenLookup.get(parentField);
        return result == null ? Collections.emptyList() : result;
    }

    public Field<?, ?, ?> getParentField(Field<MO, ?, ?> childField) {
        return childToParentLookup.get(childField);
    }

    public Set<Model<?, ?>> getDependingModels() {
        return Set.copyOf(dependingModels);
    }

    public Set<Model<?, ?>> getParentModels() {
        return Set.copyOf(parentModels);
    }

    // Returns whether this class's fields needed to be/was initialized
    protected boolean initializeIfNeeded() {
        if(isUninitialized()) {
            parentToChildrenLookup = new HashMap<>(INIT_LOOKUP_CAPACITY);
            childToParentLookup = new HashMap<>(INIT_LOOKUP_CAPACITY);
            dependingModels = new HashSet<>(INIT_LOOKUP_CAPACITY);
            parentModels = new HashSet<>(INIT_LOOKUP_CAPACITY);
            return true;
        }
        return false;
    }

    public boolean isUninitialized() {
        return parentToChildrenLookup == Collections.EMPTY_MAP
                || childToParentLookup == Collections.EMPTY_MAP
                || dependingModels == Collections.EMPTY_SET
                || parentModels == Collections.EMPTY_SET;
    }

    @Override
    public String toString() {
        return "ForeignFields{" +
                "parentModel=" + parentModel +
                ", parentToChildrenLookup=" + parentToChildrenLookup +
                ", childToParentLookup=" + childToParentLookup +
                ", dependingModels=" + dependingModels +
                ", parentModels=" + parentModels +
                '}';
    }
}
