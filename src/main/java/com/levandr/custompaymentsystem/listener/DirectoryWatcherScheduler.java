package com.levandr.custompaymentsystem.listener;


import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
//Если нужен @EnableScheduled
public class DirectoryWatcherScheduler {

    private final DirectoryWatcher directoryWatcher;

    //Если нужен @Scheduled
    @PostConstruct
    public void watch() {
        directoryWatcher.init();
    }
}
