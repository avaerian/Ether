package org.minerift.ether.util.newnbt.tags.container;

import org.minerift.ether.util.fn.IBuilder;
import org.minerift.ether.util.newnbt.tags.Tag;

@Deprecated
public interface CollectionTagBuilder<T extends Tag> extends IBuilder<T> {

    void allocateCapacity(int capacity); // can be number of elements, bytes, etc.
    int getCapacity();
    int getElementCount();
    default int getRemainingSlots() {
        return getCapacity() - getElementCount();
    }

}
