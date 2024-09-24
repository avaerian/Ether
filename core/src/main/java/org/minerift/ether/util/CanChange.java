package org.minerift.ether.util;

// Represents a class that can change over time
// Each object will have a boolean indicating whether it has changed based on when setChanged(true) is called
public abstract class CanChange {
    protected boolean changed = false;

    public void setChanged(boolean changed) {
        this.changed = changed;
    }

    public boolean hasChanged() {
        return changed;
    }
}
