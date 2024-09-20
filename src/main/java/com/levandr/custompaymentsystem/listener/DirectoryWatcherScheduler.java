package com.levandr.custompaymentsystem.listener;


import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@RequiredArgsConstructor
public class DirectoryWatcherScheduler {

    private final DirectoryWatcher directoryWatcher;

    @Scheduled(fixedDelay = 5000)
    public void watch() {
        directoryWatcher.watchDirectory();
    }
}
