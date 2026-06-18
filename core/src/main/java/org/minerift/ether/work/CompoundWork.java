package org.minerift.ether.work;

import java.util.*;
import java.util.function.BooleanSupplier;

public class CompoundWork<T> extends Work<T> {

    protected final Queue<BooleanSupplier> tasks;

    public CompoundWork(Queue<BooleanSupplier> tasks) {
        this.tasks = tasks;
    }

    public CompoundWork() {
        this.tasks = new LinkedList<>();
    }

    public Collection<BooleanSupplier> getTasksView() {
        return Collections.unmodifiableCollection(tasks);
    }

    @Override
    public boolean complete() {
        if(tasks.isEmpty())
            return true;

        if(!tasks.poll().getAsBoolean())

        return ;
    }
}
