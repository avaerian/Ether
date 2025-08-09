package org.minerift.ether.work;

public class SingleTask extends Task {

    public Runnable task; // returns whether task succeeded or failed

    public SingleTask(Runnable task) {
        this.task = task;
    }

    @Override
    protected boolean completeNextTask() {
        task.run();
        //runCallback(success ? Status.OP_COMPLETE : Status.TASK_FAILED);
        runCallback(Status.OP_COMPLETE);
        return true; // task is completed
    }
}
