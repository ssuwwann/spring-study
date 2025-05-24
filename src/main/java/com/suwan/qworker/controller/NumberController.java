package com.suwan.qworker.controller;

import com.suwan.qworker.service.RandomNumberService;
import com.suwan.qworker.worker.NumberQueueWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for handling number-related HTTP requests.
 */
@RestController
@RequestMapping("/api/numbers")
public class NumberController {
    
    private static final Logger logger = LoggerFactory.getLogger(NumberController.class);
    private final RandomNumberService randomNumberService;
    private final NumberQueueWorker queueWorker;
    
    @Autowired
    public NumberController(RandomNumberService randomNumberService, NumberQueueWorker queueWorker) {
        this.randomNumberService = randomNumberService;
        this.queueWorker = queueWorker;
    }
    
    /**
     * Endpoint to start the queue worker and generate random numbers.
     * 
     * @param count optional parameter for the number of random numbers to generate
     * @param delay optional parameter for the delay between generating numbers in milliseconds
     * @return response with status message
     */
    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> startGenerating(
            @RequestParam(required = false, defaultValue = "0") int count,
            @RequestParam(required = false, defaultValue = "1000") long delay) {
        
        randomNumberService.startGenerating(count, delay);
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "started");
        response.put("message", "Random number generation started");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Endpoint to stop the queue worker and random number generation.
     * 
     * @return response with status message and final sum
     */
    @PostMapping("/stop")
    public ResponseEntity<Map<String, Object>> stopGenerating() {
        randomNumberService.stopGenerating();
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "stopped");
        response.put("message", "Random number generation stopped");
        response.put("finalSum", queueWorker.getSum());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Endpoint to add a single number to the queue.
     * 
     * @param number the number to add to the queue
     * @return response with the added number and current sum
     */
    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addNumber(@RequestParam int number) {
        // Start the queue worker if it's not already running
        queueWorker.start();
        
        // Add the number to the queue
        queueWorker.addToQueue(number);
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "added");
        response.put("message", "Number added to queue");
        response.put("addedNumber", number);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Endpoint to add a random number to the queue.
     * 
     * @return response with the generated random number
     */
    @PostMapping("/add/random")
    public ResponseEntity<Map<String, Object>> addRandomNumber() {
        int randomNumber = randomNumberService.addSingleRandomNumber();
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "added");
        response.put("message", "Random number added to queue");
        response.put("generatedNumber", randomNumber);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Endpoint to get the current sum of processed numbers.
     * 
     * @return response with the current sum
     */
    @GetMapping("/sum")
    public ResponseEntity<Map<String, Object>> getSum() {
        Map<String, Object> response = new HashMap<>();
        response.put("sum", queueWorker.getSum());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Endpoint to reset the sum to zero.
     * 
     * @return response with status message
     */
    @PostMapping("/reset")
    public ResponseEntity<Map<String, Object>> resetSum() {
        randomNumberService.resetSum();
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "reset");
        response.put("message", "Sum reset to zero");
        
        return ResponseEntity.ok(response);
    }
}