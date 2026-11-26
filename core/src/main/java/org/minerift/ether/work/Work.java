package org.minerift.ether.work;

import org.minerift.ether.util.Either;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

@SuppressWarnings("Duplicates")
public class Work<T> {

    public static final int Wf_ENQUEUED = 0x01;
    public static final int Wf_CLOSED = 0x02;
    public static final int Wf_VALUE_PRESENT = 0x04; //needed?
    public static final int Wf_FAILED = 0x08; //needed?
    public static final int Wf_COMPLETE = 0x10;

    public static <T> Work<T> completed(String name, T value) {
        Work<T> work = new Work<>(name);
        work.result = Either.left(value);
        work.flags.getAndUpdate((f) -> f | Wf_VALUE_PRESENT);

        return work;
    }

    //TODO: understand how to build up to T
    //Each task will provide some data that is accumulated for a final result.
    //Think of this Work class as a matrix of subtasks. Each subtask may (or may not)
    //provide some data to be accumulated as a final result. Each subtask may need to depend on
    //data from all subtasks, or may not need to depend on any data and work individually
    //until all data can be accumulated at the end.



    protected WorkQueue owner;
    protected final String name;
    protected int nextTaskId;
    protected volatile Either<T, Throwable> result;
    //protected Consumer<ValueStatus> onComplete;
    protected final Queue<Consumer<Task>> tasks;
    protected Task[] activeTasks;
    protected final BitSet completedTasks;

    protected final AtomicInteger flags; //ENQUEUED, CLOSED, VALUE_PRESENT, FAILED

    protected final Queue<Consumer<Work<T>>> wrapUp;
    protected final Queue<Consumer<Work<T>>> whenFail;
    protected final Queue<Consumer<Work<T>>> whenComplete;

    public Work(String name, Queue<Consumer<Task>> tasks) {
        this.name = name;
        this.nextTaskId = 0;
        this.tasks = tasks;
        this.activeTasks = new Task[0];
        this.completedTasks = new BitSet();
        this.result = null;
        this.flags = new AtomicInteger();

        this.whenFail = new LinkedList<>();
        this.whenComplete = new LinkedList<>();
        this.wrapUp = new LinkedList<>();
    }

    /**
     * Create a new Work container to queue.
     *
     * @param name name of work container.
     */
    public Work(String name) {
        this(name, new ArrayDeque<>());
    }

    /**
     * Create a new Work container to queue.
     *
     * @param name name of work container.
     * @param taskN number of tasks expected.
     */
    public Work(String name, int taskN) {
        this(name, new ArrayDeque<>(taskN));
    }

    /**
     * Add a task to the Work container.
     *
     * <p>
     * <b>NOTE:</b> undefined behavior is expected when adding a task
     * after the Work container has been enqueued for execution.
     *
     * @param task the task.
     */
    public void addTask(Consumer<Task> task) {
        tasks.add(task);
    }

    public Collection<Consumer<Task>> getTasksView() {
        return Collections.unmodifiableCollection(tasks);
    }

    protected synchronized void markComplete(Task task) {
        completedTasks.set(task.taskId);
        activeTasks[task.taskId] = null;
        if(completedTasks.nextClearBit(0) == nextTaskId) {
            Consumer<Work<T>> cb;
            while((cb = wrapUp.poll()) != null) {
                cb.accept(this);
            }

            while((cb = whenComplete.poll()) != null) {
                cb.accept(this);
            }
        }
    }

    public synchronized void fail(Throwable e) {
        if(owner == null)
            throw new IllegalStateException("Failed Work container, but no owning WorkQueue");

        owner.logger.error("Work '{}' failed", name, e);
        this.result = Either.right(e);
        flags.getAndUpdate((f) -> f | Wf_FAILED);

        synchronized (whenFail) {
            Consumer<Work<T>> cb;
            while((cb = whenFail.poll()) != null) {
                cb.accept(this);
            }
        }
    }

    protected synchronized void fail(Task task, Throwable t, boolean failAllTasks) {
        if(owner == null)
            throw new IllegalStateException("Failed Work container, but no owning WorkQueue");

        if(!failAllTasks) {
            owner.logger.warn("Task {} failed silently", task.taskId, t);
            return;
        }
        this.result = Either.right(t);
        flags.getAndUpdate((f) -> f | Wf_FAILED);

        synchronized (whenFail) {
            Consumer<Work<T>> cb;
            while((cb = whenFail.poll()) != null) {
                cb.accept(this);
            }
        }
    }

    public synchronized void submit(T result) {
        this.result = Either.left(result);
        flags.getAndUpdate((f) -> f | Wf_VALUE_PRESENT);
    }

    /**
     * Attempts to complete this work, returning true if the work has completed.
     *
     * <p>
     * <b>NOTE:</b> the return value is <b><u>NOT</u></b> based on the status of the work queue
     * and whether it's open or not.
     *
     * @return whether the work has completed all tasks
     */
    protected boolean runNextTask(WorkQueue queue) {
        if(tasks.isEmpty()) {
            return false;
        }

        try {
            Task task = new Task(this, nextTaskId);
            activeTasks[nextTaskId++] = task;
            tasks.poll().accept(task);
        } catch (Exception e) {
            fail(e);
        }

        return true;
    }

    /**
     * Returns the result if present, else blocks the current thread until either the result
     * is provided, or the task fails for some reason (e.g. queue shutdown, invalid state).
     *
     * @throws Throwable possible throwable if at any time the work fails.
     *
     * @return result, if present.
     */
    public T result() throws Throwable {
        while (result == null)
            Thread.onSpinWait();

        if(result.isRight())
            throw result.getRight();
        return result.getLeft();
    }

    public boolean hasFailed() {
        return (flags.getAcquire() & Wf_FAILED) != 0;
    }

    public boolean hasValue() {
        return (flags.getAcquire() & Wf_VALUE_PRESENT) != 0;
    }

    public boolean isClosed() {
        return (flags.getAcquire() & Wf_CLOSED) != 0;
    }

    public boolean isQueued() {
        return (flags.getAcquire() & Wf_ENQUEUED) != 0;
    }

    public Work<T> wrapUp(Consumer<Work<T>> callback) {
        this.wrapUp.add(callback);
        return this;
    }

    public Work<T> whenComplete(Consumer<Work<T>> callback) {
        this.whenComplete.add(callback);
        return this;
    }

    public Work<T> whenFail(Consumer<Work<T>> callback) {
        this.whenFail.add(callback);
        return this;
    }
}
