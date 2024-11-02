package org.minerift.ether.work;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

// Batch of tasks associated with a single operation
public class TaskBatch extends Task {

    private Deque<BooleanSupplier> tasks;

    public TaskBatch() {
        this(new ArrayDeque<>());
    }

    public TaskBatch(Deque<BooleanSupplier> tasks) {
        super();
        this.tasks = tasks;
    }

    public TaskBatch addTask(BooleanSupplier task) {
        tasks.add(task);
        return this;
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
    @Override
    protected boolean completeNextTask() {
        BooleanSupplier task = tasks.poll();
        if(task == null) {
            runCallback(Task.Status.OP_COMPLETE);
            return true;
        }

        // If task failed, end operation
        boolean failed = !task.getAsBoolean();
        if(failed) {
            runCallback(Task.Status.TASK_FAILED);
        }
        return failed;
    }

}
