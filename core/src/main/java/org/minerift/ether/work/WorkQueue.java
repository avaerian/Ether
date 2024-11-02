package org.minerift.ether.work;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.minerift.ether.Ether;

import java.util.ArrayDeque;
import java.util.Deque;

// A work queue that distributes work over multiple ticks
public class WorkQueue {

    public static final double MAX_MILLIS_PER_TICK = 15;
    public static final int MAX_NANOS_PER_TICK = (int) (MAX_MILLIS_PER_TICK * 1E6);

    private BukkitTask bukkitTask;
    private final Deque<Task> workloadDeque;

    public WorkQueue() {
        this.workloadDeque = new ArrayDeque<>();
        this.bukkitTask = null;
    }

    public void start(BukkitTask task) {
        if(bukkitTask != null) {
            throw new UnsupportedOperationException("Work queue has already been started");
        }
        this.bukkitTask = task;
    }

    public void start() {
        start(Bukkit.getScheduler().runTaskTimer(Ether.plugin(), this::tick, 1L, 1L));
    }

    // Stops the work queue from completing work
    public void stop() {
        bukkitTask.cancel();
        this.bukkitTask = null;
    }

    public void close() {
        stop();
        workloadDeque.forEach(op -> op.fail(Task.Status.QUEUE_SHUTDOWN));
    }

    public synchronized void enqueue(Task operation) {
        workloadDeque.add(operation);
    }

    public Task getCurrentOperation() {
        return workloadDeque.peek();
    }

    // Runs every tick
    // Completes as much work as possible within each tick
    private void tick() {
        final long stopTime = System.nanoTime() + MAX_NANOS_PER_TICK;

        Task nextWork;
        while(System.nanoTime() <= stopTime && (nextWork = workloadDeque.peek()) != null) {
            // complete tasks for operation and poll next
            boolean finish = nextWork.completeNextTask();
            if(finish) {
                workloadDeque.poll();
            }
        }
    }
}
