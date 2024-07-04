package com.gintel.cognitiveservices.internal;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskExecutorService {
    private static TaskExecutorService instance;

    private final ExecutorService executor;

    private TaskExecutorService() {
        executor = Executors.newFixedThreadPool(10);
    }

    public static synchronized TaskExecutorService getInstance() {
        if (instance == null) {
            instance = new TaskExecutorService();
        }
        return instance;
    }

    public void submit(Runnable r) {
        executor.submit(r);
    }
}
