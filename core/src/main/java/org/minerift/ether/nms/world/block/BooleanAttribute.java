package org.minerift.ether.nms.world.block;

import java.util.List;

public class BooleanAttribute extends Attribute<Boolean> {

    protected BooleanAttribute(int id, String name) {
        super(id, name, Boolean.class);
    }

    @Override
    public boolean isAcceptableValue(Boolean val) {
        return val != null;
    }

    @Override
    public List<Boolean> getAcceptableValues() {
        return List.of(Boolean.TRUE, Boolean.FALSE);
    }
}
