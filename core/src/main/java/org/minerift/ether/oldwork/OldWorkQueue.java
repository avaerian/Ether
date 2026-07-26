package org.minerift.ether.oldwork;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.minerift.ether.EtherPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Deque;

// A work queue that distributes work over multiple ticks synchronously
public class OldWorkQueue {



    public static final double MAX_MILLIS_PER_TICK = 15;
    public static final int MAX_NANOS_PER_TICK = (int) (MAX_MILLIS_PER_TICK * 1E6);

    private Logger logger;
    private BukkitTask bukkitTask;
    private final Deque<Operation> workloadDeque;

    public OldWorkQueue(Logger logger) {
        this.workloadDeque = new ArrayDeque<>();
        this.bukkitTask = null;
        this.logger = logger;
    }

    public OldWorkQueue() {
        this(LoggerFactory.getLogger(OldWorkQueue.class));
    }

    public void start(BukkitTask task) {
        if(bukkitTask != null) {
            throw new UnsupportedOperationException("Work queue has already been started");
        }
        this.bukkitTask = task;
    }

    public void start() {
        start(Bukkit.getScheduler().runTaskTimer(EtherPlugin.getInstance(), this::tick, 1L, 1L));
    }

    // Stops the work queue from completing work
    public void stop() {
        bukkitTask.cancel();
        this.bukkitTask = null;
    }

    public void close() {
        stop();
        for(Operation op : workloadDeque)
            logger.warn("Failed task for {} because queue is closing", op.name());
        workloadDeque.clear();
    }

    public synchronized void enqueue(Operation op) {
        workloadDeque.add(op);
    }

    public Operation getCurrentOperation() {
        return workloadDeque.peek();
    }

    // Runs every tick
    // Completes as much work as possible within each tick
    private void tick() {
        final long stopTime = System.nanoTime() + MAX_NANOS_PER_TICK;

        Operation nextWork;
        while(System.nanoTime() <= stopTime && (nextWork = workloadDeque.peek()) != null) {
            // complete tasks for operation and poll next
            nextWork.task().run();
            workloadDeque.poll();
        }
    }

}
