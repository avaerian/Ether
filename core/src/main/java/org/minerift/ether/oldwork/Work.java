package org.minerift.ether.oldwork;

import com.google.common.base.Preconditions;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public abstract class Work {

    public static SingleWork of(Runnable task) {
        return new SingleWork(task);
    }

    public static BatchedWork batch() {
        return new BatchedWork();
    }

    protected CompletableFuture<Void> future;
    protected Consumer<Status> callback;

    protected Work() {
        this.callback = null;
        this.future = new CompletableFuture<>();
    }

    public CompletableFuture<Void> getFuture() {
        return future;
    }

    /**
     * Completes the next task from a simple or batched task.
     * @return boolean indicating whether the task finished
     */
    public abstract boolean completeNextTask();

    public Work whenComplete(Consumer<Work.Status> callback) {
        this.callback = callback;
        return this;
    }

    // Fail the operation
    public void fail(Work.Status reason) {
        Preconditions.checkNotNull(reason, "Fail status cannot be null!");
        Preconditions.checkArgument(reason.isFailure(), "Status must be a fail status");
        runCallback(reason);
    }

    protected void runCallback(Work.Status status) {
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
