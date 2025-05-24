package com.suwan.qworker.worker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * A queue worker implementation that processes integer values.
 * It sums the values and outputs the result along with the thread name.
 */
@Component
public class NumberQueueWorker extends QueueWorker<Integer> {
    
    private static final Logger logger = LoggerFactory.getLogger(NumberQueueWorker.class);
    private final AtomicInteger sum = new AtomicInteger(0);
    private volatile boolean running = false;
    
    /**
     * Creates a NumberQueueWorker with a thread pool of size 4.
     */
    public NumberQueueWorker() {
        super(4); // Thread pool size of 4 as per requirements
    }
    
    @Override
    public void start() {
        if (running) {
            return;
        }
        
        running = true;
        
        // Start worker threads
        for (int i = 0; i < 4; i++) {
            executorService.submit(() -> {
                while (running) {
                    try {
                        Integer value = queue.take(); // Blocking operation
                        processItem(value);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        logger.error("Worker thread interrupted", e);
                        break;
                    }
                }
            });
        }
    }
    
    @Override
    protected void processItem(Integer item) {
        int currentSum = sum.addAndGet(item);
        String threadName = Thread.currentThread().getName();
        logger.info("Thread: {}, Added: {}, Current Sum: {}", threadName, item, currentSum);
    }
    
    @Override
    public void stop() {
        running = false;
        super.stop();
    }
    
    /**
     * Gets the current sum of processed values.
     * 
     * @return the current sum
     */
    public int getSum() {
        return sum.get();
    }
    
    /**
     * Resets the sum to zero.
     */
    public void resetSum() {
        sum.set(0);
    }
}