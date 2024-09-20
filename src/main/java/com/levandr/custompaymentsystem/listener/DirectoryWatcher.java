package com.levandr.custompaymentsystem.listener;

import com.levandr.custompaymentsystem.service.parser.FileParser;
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
    private final FileParser fileParser;
    private final Set<Path> processedFiles = new HashSet<>();


    public void init() {
        log.info("Input directory: {} ", INPUT_DIRECTORY);
        processExistingFiles();
        watchDirectory();
    }


    private void processExistingFiles() {
        log.info("Start processExistingFiles...");
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(INPUT_DIRECTORY)) {
            for (Path entry : stream) {
                if (Files.isRegularFile(entry)) {
                    synchronized (processedFiles) {
                        if (!processedFiles.contains(entry)) {
                            System.out.println("Processing existing file: " + entry);
                            fileParser.parseFile(entry);
                            processedFiles.add(entry);
                        }
                    }
                }
            }
        } catch (IOException e) {
            log.error("IO exception in processExcFile", e);
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
                                log.info("File created: {}", filePath);
                                fileParser.parseFile(filePath);
                                processedFiles.add(filePath);
                            }
                        }
                    }
                }
                key.reset();
            }
        } catch (IOException exception) {
            log.error("Error watching directory", exception);
        }
    }
}
