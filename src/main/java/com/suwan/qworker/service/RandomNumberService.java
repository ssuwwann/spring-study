package com.suwan.qworker.service;

import com.suwan.qworker.worker.NumberQueueWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Service for generating random numbers and adding them to the queue worker.
 */
@Service
public class RandomNumberService {
    
    private static final Logger logger = LoggerFactory.getLogger(RandomNumberService.class);
    private final NumberQueueWorker queueWorker;
    private final Random random = new Random();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private volatile boolean generating = false;
    
    @Autowired
    public RandomNumberService(NumberQueueWorker queueWorker) {
        this.queueWorker = queueWorker;
    }
    
    /**
     * Starts generating random numbers and adding them to the queue.
     * 
     * @param count the number of random numbers to generate
     * @param delayMillis the delay between generating numbers in milliseconds
     */
    public void startGenerating(int count, long delayMillis) {
        if (generating) {
            return;
        }
        
        generating = true;
        queueWorker.start();
        
        scheduler.scheduleAtFixedRate(() -> {
            if (count > 0 && queueWorker.getSum() >= count) {
                stopGenerating();
                return;
            }
            
            int randomNumber = random.nextInt(1001); // 0 to 1000
            queueWorker.addToQueue(randomNumber);
            logger.info("Generated random number: {}", randomNumber);
        }, 0, delayMillis, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Stops generating random numbers.
     */
    public void stopGenerating() {
        if (!generating) {
            return;
        }
        
        generating = false;
        scheduler.shutdown();
        queueWorker.stop();
        logger.info("Stopped generating random numbers. Final sum: {}", queueWorker.getSum());
    }
    
    /**
     * Adds a single random number to the queue.
     * 
     * @return the generated random number
     */
    public int addSingleRandomNumber() {
        int randomNumber = random.nextInt(1001); // 0 to 1000
        queueWorker.addToQueue(randomNumber);
        logger.info("Added single random number: {}", randomNumber);
        return randomNumber;
    }
    
    /**
     * Resets the queue worker's sum.
     */
    public void resetSum() {
        queueWorker.resetSum();
    }
}