package org.minerift.ether.work;

import com.google.common.collect.ImmutableList;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.Callable;
import java.util.function.BooleanSupplier;

// Batch of tasks associated with a single operation
// TODO: refactor WorkQueue system for better clarity and exception handling
public class TaskBatch extends Task {

    private Deque<Callable<Void>> tasks;

    public TaskBatch() {
        this(new ArrayDeque<>());
    }

    public TaskBatch(Deque<Callable<Void>> tasks) {
        super();
        this.tasks = tasks;
    }

    public TaskBatch addTask(Callable<Void> task) {
        tasks.add(task);
        return this;
    }

    // Append all tasks from other operation to this operation
    public TaskBatch join(TaskBatch other) {
        tasks.addAll(other.tasks);
        return this;
    }

    public ImmutableList<Callable<Void>> getRemainingTasks() {
        return ImmutableList.copyOf(tasks);
    }

    public int getRemainingTaskCount() {
        return tasks.size();
    }

    // Completes a single task
    // Returns whether the operation has finished
    @Override
    protected boolean completeNextTask() {
        Callable<Void> task = tasks.poll();
        if(task == null) {
            runCallback(Task.Status.OP_COMPLETE);
            return true;
        }

        try {
            task.call();
            return true;
        } catch (Exception ex) {
            runCallback(Task.Status.TASK_FAILED);
            return false;
        }
    }

}
