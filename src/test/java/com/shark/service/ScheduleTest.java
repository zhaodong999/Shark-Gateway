package com.shark.service;

import org.junit.jupiter.api.Test;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ScheduleTest {

    @Test
    public void test() throws InterruptedException {
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        ScheduledFuture<?> scheduledFuture = service.scheduleAtFixedRate(this::pollLoadNewService, 1, 10, TimeUnit.SECONDS);
        System.out.println("end");
        Thread.currentThread().join();
    }

    private void pollLoadNewService() {
        System.out.println("test");
    }
}
