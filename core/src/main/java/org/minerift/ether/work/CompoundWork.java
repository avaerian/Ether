package org.minerift.ether.work;

import org.minerift.ether.util.Either;

import java.util.*;
import java.util.function.BooleanSupplier;

public class CompoundWork<T> extends Work<T> {

    protected final Queue<BooleanSupplier> tasks;

    public CompoundWork(String name, Queue<BooleanSupplier> tasks) {
        super(name);
        this.tasks = tasks;
    }

    public CompoundWork(String name) {
        super(name);
        this.tasks = new LinkedList<>();
    }

    public Collection<BooleanSupplier> getTasksView() {
        return Collections.unmodifiableCollection(tasks);
    }

    @Override
    public boolean complete() {
        if(tasks.isEmpty()) {
            status = ValueStatus.PRESENT;
            return true;
        }

        if(!tasks.poll().getAsBoolean()) {
            status = ValueStatus.FAILED;
            return true;
        }

        return false;
    }
}
