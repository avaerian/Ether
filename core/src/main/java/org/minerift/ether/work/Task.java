package org.minerift.ether.work;

import com.google.common.base.Preconditions;

import java.util.concurrent.CompletableFuture;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public abstract class Task {

    public static SingleTask of(Runnable task) {
        return new SingleTask(task);
    }

    public static BatchedTask batch() {
        return new BatchedTask();
    }

    protected Consumer<Status> callback;

    protected Task() {
        this.callback = null;
    }

    /**
     * Completes the next task from a simple or batched task.
     * @return boolean indicating whether the task finished
     */
    protected abstract boolean completeNextTask();

    public Task whenComplete(Consumer<Task.Status> callback) {
        this.callback = callback;
        return this;
    }

    // Fail the operation
    public void fail(Task.Status reason) {
        Preconditions.checkNotNull(reason, "Fail status cannot be null!");
        Preconditions.checkArgument(reason.isFailure(), "Status must be a fail status");
        runCallback(reason);
    }

    protected void runCallback(Task.Status status) {
        if(callback != null) {
            callback.accept(status);
        }
    }

    public enum Status {
        OP_COMPLETE, // all tasks completed successfully
        QUEUE_SHUTDOWN, // operation failed because work queue shutdown
        TASK_FAILED, // operation failed because task failed

        ;

        public boolean isComplete() {
            return this == OP_COMPLETE;
        }

        public boolean isFailure() {
            return switch(this) {
                case QUEUE_SHUTDOWN, TASK_FAILED -> true;
                default -> false;
            };
        }
    }
}
