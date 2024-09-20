package com.levandr.custompaymentsystem.listener;


import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@EnableScheduling
@RequiredArgsConstructor
public class DirectoryWatcherScheduler {

    private final DirectoryWatcher directoryWatcher;
    private final ExecutorService executorService = Executors.newThreadPerTaskExecutor(Thread.ofVirtual().factory());

    @Scheduled(fixedDelay = 5000)
    @PostConstruct
    public void watch() {
        executorService.submit(directoryWatcher::init);
    }
}
