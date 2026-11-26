package org.minerift.ether.work;

public class Task {

    private final Work<?> work;
    protected final int taskId;

    protected Task(Work<?> work, int taskId) {
        this.work = work;
        this.taskId = taskId;
    }

    public void complete() {
        work.markComplete(this);
    }

    public void fail(Throwable ex) {
        fail(ex, true);
    }

    public void fail(Throwable ex, boolean failAllTasks) {
        work.fail(this, ex, failAllTasks);
    }

}
