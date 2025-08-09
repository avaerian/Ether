package org.minerift.ether.work;

import com.google.common.collect.ImmutableList;

import java.util.ArrayDeque;
import java.util.Deque;

// Batch of tasks associated with a single operation
// TODO: refactor WorkQueue system for better clarity and exception handling
public class BatchedTask extends Task {

    private Deque<Runnable> tasks;

    public BatchedTask() {
        this(new ArrayDeque<>());
    }

    public BatchedTask(Deque<Runnable> tasks) {
        super();
        this.tasks = tasks;
    }

    public BatchedTask addTask(Runnable task) {
        tasks.add(task);
        return this;
    }

    // Append all tasks from other operation to this operation
    public BatchedTask join(BatchedTask other) {
        tasks.addAll(other.tasks);
        return this;
    }

    public ImmutableList<Runnable> getRemainingTasks() {
        return ImmutableList.copyOf(tasks);
    }

    public int getRemainingTaskCount() {
        return tasks.size();
    }

    // Completes a single task
    // Returns whether the operation has finished
    @Override
    protected boolean completeNextTask() {
        Runnable task = tasks.poll();
        if(task == null) {
            runCallback(Task.Status.OP_COMPLETE);
            return true;
        }

        /*try {
            task.run();
        } catch (Exception ex) {
            runCallback(Operation.Status.TASK_FAILED);
        }*/
        task.run();
        return false;
    }

}
