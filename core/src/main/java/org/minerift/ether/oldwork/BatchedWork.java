package org.minerift.ether.oldwork;

import com.google.common.collect.ImmutableList;

import java.util.ArrayDeque;
import java.util.Deque;

// Batch of tasks associated with a single operation
// TODO: refactor WorkQueue system for better clarity and exception handling
public class BatchedWork extends Work {

    private Deque<Runnable> tasks;

    public BatchedWork() {
        this(new ArrayDeque<>());
    }

    public BatchedWork(Deque<Runnable> tasks) {
        super();
        this.tasks = tasks;
    }

    public BatchedWork addTask(Runnable task) {
        tasks.add(task);
        return this;
    }

    // Append all tasks from other operation to this operation
    public BatchedWork join(BatchedWork other) {
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
    public boolean completeNextTask() {
        Runnable task = tasks.poll();
        if(task == null) {
            future.complete(null);
            runCallback(Work.Status.OP_COMPLETE);
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
