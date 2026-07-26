package org.minerift.ether.work;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.minerift.ether.EtherPlugin;
import org.minerift.ether.util.Either;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

public class WorkQueue {

    public static long MAX_MS_PER_TICK = 15;
    public static long MAX_NANOS_PER_TICK = TimeUnit.MILLISECONDS.toNanos(MAX_MS_PER_TICK);


    private BukkitTask task;
    private Queue<Work<?>> queue;

    public WorkQueue() {
        this.task = Bukkit.getScheduler().runTaskTimer(EtherPlugin.getInstance(), this::tick, 1L, 1L);
        this.queue = new LinkedList<>();
    }

    private void tick() {
        final long endNanos = System.nanoTime() + MAX_NANOS_PER_TICK;
        Work<?> work;
        while(System.nanoTime() <= endNanos && (work = queue.peek()) != null) {
            if(work.complete())
                queue.poll();
        }
    }

    public void enqueue(Work<?> work) {
        // TODO
    }

    public void start() {

    }

    public void close() {
    }
}
