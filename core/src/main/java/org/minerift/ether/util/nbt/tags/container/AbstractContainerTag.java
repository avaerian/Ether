package org.minerift.ether.util.nbt.tags.container;

import org.jetbrains.annotations.NotNull;
import org.minerift.ether.util.nbt.tags.Tag;

import java.util.function.UnaryOperator;

// C -> container type
public abstract class AbstractContainerTag<C> extends Tag {

    public abstract Tag copy(@NotNull UnaryOperator<C> copyContainerFn);
    public abstract int size();

}
