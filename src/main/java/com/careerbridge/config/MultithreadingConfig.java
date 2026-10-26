package com.careerbridge.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Multithreading Configuration for CareerBridge.
 * 
 * Provides an explicit ThreadPoolTaskExecutor to manage concurrent worker threads
 * for asynchronous operations (such as batch Auto-Apply job submissions).
 * 
 * Key Architecture Properties:
 * 1. Thread Pool Management: Reuses a fixed pool of worker threads instead of spawning unbounded threads.
 * 2. Core Pool Size: 5 active worker threads always ready to process tasks.
 * 3. Max Pool Size: Bursts up to 10 concurrent threads under peak load.
 * 4. Task Queue Buffer: Holds up to 100 pending asynchronous tasks.
 * 5. Thread Name Prefix: "CareerBridge-Async-X" for thread identification in runtime logs.
 */
@Configuration
@EnableAsync
public class MultithreadingConfig {

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);        // 5 active worker threads
        executor.setMaxPoolSize(10);       // Bursts up to 10 threads under load
        executor.setQueueCapacity(100);    // Task queue buffer
        executor.setThreadNamePrefix("CareerBridge-Async-");
        executor.initialize();
        return executor;
    }
}
