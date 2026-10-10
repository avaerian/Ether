package org.minerift.ether.work;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.minerift.ether.Ether;
import org.minerift.ether.EtherPlugin;
import org.slf4j.Logger;

import java.util.ArrayDeque;
import java.util.BitSet;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

public class WorkQueue {

    public static long MAX_MS_PER_TICK = 15;
    public static long MAX_NANOS_PER_TICK = TimeUnit.MILLISECONDS.toNanos(MAX_MS_PER_TICK);


    private final String name;
    private final JavaPlugin plugin; // for spawning Bukkit tasks to complete work
    protected final Logger logger;
    private BukkitTask task;
    private Queue<Work<?>> queue;
    protected BitSet activeWork;


    public WorkQueue(String name, JavaPlugin plugin, Logger logger) {
        this.name = name;
        this.queue = new ArrayDeque<>();
        this.logger = logger;
        this.task = null;
        this.plugin = plugin;
    }

    public WorkQueue(String name, Logger logger) {
        this(name, EtherPlugin.getInstance(), logger);
    }

    private void tick() {
        final long endNanos = System.nanoTime() + MAX_NANOS_PER_TICK;
        Work<?> work;
        while(System.nanoTime() <= endNanos && (work = queue.peek()) != null) {
            if(!work.runNextTask(this))
                queue.poll();
        }
    }

    public synchronized void enqueue(Work<?> work) {
        work.activeTasks = new Task[work.tasks.size()];
        queue.add(work);
    }

    public boolean start() {
        boolean isStopped = task == null;
        if(!isStopped) {
            this.task = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 1L, 1L);
        }
        return isStopped;
    }

    public boolean stop() {
        boolean isRunning = task != null;
        if(isRunning) {
            task.cancel();
        }
        task = null;
        return isRunning;
    }

    public void close() {
        stop();
        queue.forEach(work -> work.fail(new WorkQueueException("Work queue shutdown")));
    }

    public Work<?> getCurrentWork() {
        return queue.peek();
    }
}