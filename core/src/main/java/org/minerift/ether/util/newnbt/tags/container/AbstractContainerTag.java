package org.minerift.ether.util.newnbt.tags.container;

import com.google.common.base.Preconditions;
import org.minerift.ether.util.newnbt.NBT;
import org.minerift.ether.util.newnbt.tags.Tag;

public abstract class AbstractContainerTag<V> extends Tag<V> {

    @Deprecated
    public static AbstractContainerTag<?> newContainerTag(NBT.TagType containerType) {
        Preconditions.checkNotNull(containerType);
        AbstractContainerTag<?> tag = switch(containerType) {
            case COMPOUND_TAG -> new CompoundTag("");
            case LIST_TAG -> ListTag.sizeKnownLater("");
            default -> throw new IllegalArgumentException(containerType.getName() + " is not a container tag!");
        };
        return tag;
    }

    public abstract void addTag(Tag<?> tag);
    public abstract void removeTag(Tag<?> tag);

}
