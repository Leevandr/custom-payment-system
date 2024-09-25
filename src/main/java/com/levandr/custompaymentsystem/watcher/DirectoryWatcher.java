package com.levandr.custompaymentsystem.watcher;

import com.levandr.custompaymentsystem.exception.FileProcessingException;
import com.levandr.custompaymentsystem.service.parser.ParserService;
import jakarta.annotation.PostConstruct;
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
    private Path inputDirectory;
    private final ParserService parserService;
    private final Set<Path> processedFiles = new HashSet<>();

    @PostConstruct
    public void init() {
        log.info("INIT Input directory: {} ", inputDirectory);
        processExistingFiles();
        watchDirectory();
    }

    @Async
    public void processExistingFiles() {
        log.info("Start processExistingFiles...");
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(inputDirectory)) {
            for (Path entry : stream) {
                if (Files.isRegularFile(entry)) {
                    processFile(entry);
                }
            }
        } catch (IOException | FileProcessingException e) {
            throw new RuntimeException("Failed to access directory: " + inputDirectory, e);
        }
    }

    @Async
    public void watchDirectory() {
        log.info("Start watch directory...");
        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
            inputDirectory.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
            log.info("WatchService registered for directory: {}", inputDirectory);

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
                        Path filePath = inputDirectory.resolve((Path) event.context());
                        log.info("New file detected: {}", filePath);
                        processFile(filePath);
                    }
                }
                key.reset();
            }
        } catch (IOException | FileProcessingException e) {
            log.error("Error while setting up the directory watcher: {}", e.getMessage());
            throw new RuntimeException("Failed to set up directory watcher for: " + inputDirectory, e);
        }
    }

    private void processFile(Path filePath) throws FileProcessingException, IOException {
        synchronized (processedFiles) {
            if (!processedFiles.contains(filePath)) {
                log.info("Processing file: {}", filePath);
                parserService.parseFile(filePath);
                processedFiles.add(filePath);
            }
        }
    }
}
