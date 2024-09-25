package com.levandr.custompaymentsystem.watcher;

import com.levandr.custompaymentsystem.exception.FileProcessingException;
import com.levandr.custompaymentsystem.service.parser.ParserService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class DirectoryWatcher {

    private static final Logger log = LoggerFactory.getLogger(DirectoryWatcher.class);
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);

    @Value("${spring.input.directory}")
    private Path inputDirectory;

    private final ParserService parserService;
    private final Set<Path> processedFiles = new HashSet<>();

    @PostConstruct
    public void init() {
        log.info("Initializing directory watcher for: {}", inputDirectory);
        processExistingFiles();
        startDirectoryWatch();
    }

    public void processExistingFiles() {
        log.info("Processing existing files...");
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(inputDirectory)) {
            for (Path entry : stream) {
                if (Files.isRegularFile(entry)) {
                    processFileAsync(entry);
                }
            }
        } catch (IOException e) {
            log.error("Failed to access directory: {}", inputDirectory, e);
        }
    }

    public void startDirectoryWatch() {
        log.info("Starting to watch directory...");
        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
            inputDirectory.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
            log.info("Directory watcher registered for: {}", inputDirectory);

            executorService.submit(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    WatchKey key;
                    try {
                        key = watchService.poll(10, TimeUnit.SECONDS);  // Периодическое ожидание событий
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.warn("WatchService interrupted", e);
                        break;
                    }

                    if (key != null) {
                        for (WatchEvent<?> event : key.pollEvents()) {
                            WatchEvent.Kind<?> kind = event.kind();
                            if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                                Path filePath = inputDirectory.resolve((Path) event.context());
                                log.info("New file detected: {}", filePath);
                                processFileAsync(filePath);
                            }
                        }
                        key.reset();
                    }
                }
            });
        } catch (IOException e) {
            log.error("Failed to set up directory watcher: {}", e.getMessage());
            throw new RuntimeException("Failed to set up directory watcher for: " + inputDirectory, e);
        }
    }

    private void processFileAsync(Path filePath) {
        synchronized (processedFiles) {
            if (!processedFiles.contains(filePath)) {
                executorService.submit(() -> {
                    try {
                        log.info("Processing file: {}", filePath);
                        parserService.parseFile(filePath);
                        processedFiles.add(filePath);
                    } catch (FileProcessingException e) {
                        log.error("Error processing file: {}", filePath, e);
                    }
                });
            }
        }
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down directory watcher...");
        try {
            executorService.shutdown();
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                log.warn("Forcing shutdown of executor service.");
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.error("Error during shutdown: ", e);
            executorService.shutdownNow();
        }
    }
}
