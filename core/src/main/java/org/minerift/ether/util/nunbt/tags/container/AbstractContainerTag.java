package org.minerift.ether.util.nunbt.tags.container;

import org.jetbrains.annotations.NotNull;
import org.minerift.ether.util.nunbt.tags.Tag;

import java.util.function.Function;

// C -> container
public abstract class AbstractContainerTag<C> extends Tag<C> {

    public abstract void addTag(Tag<?> tag);
    public abstract void removeTag(Tag<?> tag);

    public abstract Tag<C> copy(@NotNull Function<C, C> copyContainerFn);
}
