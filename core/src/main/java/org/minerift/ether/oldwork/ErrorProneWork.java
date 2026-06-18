package org.minerift.ether.oldwork;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ErrorProneWork extends Work {

    private final Queue<MayThrow> tasks;

    public ErrorProneWork() {
        this.tasks = new ConcurrentLinkedQueue<>();
    }

    public void addTask(MayThrow task) {
        tasks.add(task);
    }

    @Override
    public boolean completeNextTask() {
        MayThrow task = tasks.poll();
        if(task == null) {
            future.complete(null);
            runCallback(Status.OP_COMPLETE);
            return true;
        }

        try {
            task.call();
        } catch (Exception e) {
            future.completeExceptionally(e);
            runCallback(Status.TASK_FAILED);
            return true;
        }
        return false;
    }
}
