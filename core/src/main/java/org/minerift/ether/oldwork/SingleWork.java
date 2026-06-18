package org.minerift.ether.oldwork;

import java.util.concurrent.CompletableFuture;

public class SingleWork extends Work {

    public Runnable task; // returns whether task succeeded or failed

    public SingleWork(Runnable task) {
        this.task = task;
        this.future = new CompletableFuture<>();
    }

    @Override
    public boolean completeNextTask() {
        task.run();
        future.complete(null);
        //runCallback(success ? Status.OP_COMPLETE : Status.TASK_FAILED);
        runCallback(Status.OP_COMPLETE);
        return true; // task is completed
    }
}
