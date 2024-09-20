package com.levandr.custompaymentsystem.listener;


import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@RequiredArgsConstructor
public class DirectoryWatcherScheduler {

    private final DirectoryWatcher directoryWatcher;

    @PostConstruct
    public void watch() {
        directoryWatcher.init();
    }
}
