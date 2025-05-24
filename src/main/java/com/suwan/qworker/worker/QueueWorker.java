package com.suwan.qworker.worker;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Abstract class for queue worker implementations.
 * Provides basic functionality for managing a thread pool and a queue.
 */
public abstract class QueueWorker<T> {
    
    protected final ExecutorService executorService;
    protected final BlockingQueue<T> queue;
    
    /**
     * Creates a QueueWorker with the specified number of threads.
     * 
     * @param threadCount the number of threads in the thread pool
     */
    public QueueWorker(int threadCount) {
        this.executorService = Executors.newFixedThreadPool(threadCount);
        this.queue = new LinkedBlockingQueue<>();
    }
    
    /**
     * Adds an item to the queue.
     * 
     * @param item the item to add
     */
    public void addToQueue(T item) {
        queue.add(item);
    }
    
    /**
     * Starts processing items from the queue.
     */
    public abstract void start();
    
    /**
     * Stops the worker and shuts down the thread pool.
     */
    public void stop() {
        executorService.shutdown();
    }
    
    /**
     * Processes a single item from the queue.
     * 
     * @param item the item to process
     */
    protected abstract void processItem(T item);
}