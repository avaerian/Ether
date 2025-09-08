package org.minerift.ether.work.deprecated;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ErrorProneTask extends Task {

    private final Queue<MayThrow> tasks;

    public ErrorProneTask() {
        this.tasks = new ConcurrentLinkedQueue<>();
    }

    public void addTask(MayThrow task) {
        tasks.add(task);
    }

    @Override
    public boolean completeNextTask() {
        return false;
    }
}
