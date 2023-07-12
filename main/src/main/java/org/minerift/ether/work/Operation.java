package org.minerift.ether.work;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;

// Batch of work that is completed together
public class Operation {

    private Deque<BooleanSupplier> tasks;
    private BiConsumer<Boolean, FailReason> callback;

    public Operation() {
        this(new ArrayDeque<>());
    }

    public Operation(Deque<BooleanSupplier> tasks) {
        this.tasks = tasks;
        this.callback = null;
    }

    public void addTask(BooleanSupplier task) {
        tasks.add(task);
    }

    public void whenComplete(BiConsumer<Boolean, FailReason> callback) {
        this.callback = callback;
    }

    private void runCallback(boolean finished, FailReason failReason) {
        if(callback != null) {
            callback.accept(finished, failReason);
        }
    }

    // Fail the operation
    public void fail(FailReason reason) {
        Preconditions.checkNotNull(reason, "Fail reason cannot be null!");
        runCallback(false, reason);
    }

    // Append all tasks from other operation to this operation
    public void join(Operation other) {
        tasks.addAll(other.tasks);
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
        BooleanSupplier task;
        if((task = tasks.poll()) == null) {
            runCallback(true, null);
            return true;
        }

        // If task failed, end operation
        boolean failed = !task.getAsBoolean();
        if(failed) {
            runCallback(false, FailReason.TASK_FAILED);
        }
        return failed;
    }

    public enum FailReason {
        QUEUE_SHUTDOWN,
        TASK_FAILED,
        UNSPECIFIED
    }
}
