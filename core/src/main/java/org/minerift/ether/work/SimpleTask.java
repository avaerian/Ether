package org.minerift.ether.work;

import java.util.function.BooleanSupplier;

public class SimpleTask extends Task {

    public BooleanSupplier task; // returns whether task succeeded or failed

    public SimpleTask(BooleanSupplier task) {
        this.task = task;
    }

    @Override
    protected boolean completeNextTask() {
        boolean success = task.getAsBoolean();
        runCallback(success ? Status.OP_COMPLETE : Status.TASK_FAILED);
        return true; // task is completed
    }
}
