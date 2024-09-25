package com.levandr.custompaymentsystem.listener;

import com.levandr.custompaymentsystem.exception.FileProcessingException;
import com.levandr.custompaymentsystem.service.parser.ParserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;
import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DirectoryWatcher {

    private static final Logger log = LoggerFactory.getLogger(DirectoryWatcher.class);
    @Value("${spring.input.directory}")
    private Path INPUT_DIRECTORY;
    private final ParserService parserService;
    private final Set<Path> processedFiles = new HashSet<>();

    public void init() {
        log.info("INIT Input directory: {} ", INPUT_DIRECTORY);
        processExistingFiles();
        watchDirectory();
    }

    @Async
    public void processExistingFiles() {
        log.info("Start processExistingFiles...");
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(INPUT_DIRECTORY)) {
            for (Path entry : stream) {
                if (Files.isRegularFile(entry)) {
                    synchronized (processedFiles) {
                        if (!processedFiles.contains(entry)) {
                            log.info("Processing existing file: {} ", entry);
                            parserService.parseFile(entry);
                            processedFiles.add(entry);
                        }
                    }
                }
            }
        } catch (IOException | FileProcessingException e) {
            throw new RuntimeException("Failed to access directory: " + INPUT_DIRECTORY, e);
        }
    }

    @Async
    public void watchDirectory() {
        log.info("Start watch directory...");
        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
            INPUT_DIRECTORY.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
            log.info("WatchService registered for directory: {}", INPUT_DIRECTORY);

            while (true) {
                WatchKey key;
                try {
                    key = watchService.take();
                } catch (InterruptedException e) {
                    log.error("WatchService interrupted", e);
                    return;
                }
                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();

                    if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                        Path filePath = INPUT_DIRECTORY.resolve((Path) event.context());
                        log.info("New file detected: {}", filePath);
                        synchronized (processedFiles) {
                            if (!processedFiles.contains(filePath)) {
                                parserService.parseFile(filePath);
                                processedFiles.add(filePath);
                            }
                        }
                    }
                }
                key.reset();
            }
        } catch (IOException | FileProcessingException e) {
            log.error("Error while setting up the directory watcher: {}", e.getMessage());
            throw new RuntimeException("Failed to set up directory watcher for: " + INPUT_DIRECTORY, e);
        }
    }
}
