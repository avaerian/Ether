package org.minerift.ether.work;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

// Batch of tasks associated with a single operation
public class TaskBatch {

    private Deque<BooleanSupplier> tasks;
    private Consumer<Status> callback;

    public TaskBatch() {
        this(new ArrayDeque<>());
    }

    public TaskBatch(Deque<BooleanSupplier> tasks) {
        this.tasks = tasks;
        this.callback = null;
    }

    public TaskBatch addTask(BooleanSupplier task) {
        tasks.add(task);
        return this;
    }

    public TaskBatch whenComplete(Consumer<Status> callback) {
        this.callback = callback;
        return this;
    }

    protected void runCallback(Status status) {
        if(callback != null) {
            callback.accept(status);
        }
    }

    // Fail the operation
    public void fail(Status reason) {
        Preconditions.checkNotNull(reason, "Fail status cannot be null!");
        Preconditions.checkArgument(reason.isFailure(), "Status must be a fail status");
        runCallback(reason);
    }

    // Append all tasks from other operation to this operation
    public TaskBatch join(TaskBatch other) {
        tasks.addAll(other.tasks);
        return this;
    }

    public ImmutableList<BooleanSupplier> getRemainingTasks() {
        return ImmutableList.copyOf(tasks);
    }

    public int getRemainingTaskCount() {
        return tasks.size();
    }

    // Completes a single task
    // Returns whether the operation has finished
    protected boolean completeNextTask() {
        BooleanSupplier task = tasks.poll();
        if(task == null) {
            runCallback(Status.OP_COMPLETE);
            return true;
        }

        // If task failed, end operation
        boolean failed = !task.getAsBoolean();
        if(failed) {
            runCallback(Status.TASK_FAILED);
        }
        return failed;
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
