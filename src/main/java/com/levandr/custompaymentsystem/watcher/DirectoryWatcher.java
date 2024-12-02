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
    final ExecutorService executorService = Executors.newFixedThreadPool(5);

    @Value("${spring.input.directory}")
    private Path inputDirectory;

    private final ParserService parserService;
    final Set<Path> processedFiles = new HashSet<>();

    @PostConstruct
    public void init() {
        log.info("Инициализация наблюдателя каталогов для: {}", inputDirectory);
        processExistingFiles();
        startDirectoryWatch();
    }

    public void processExistingFiles() {
        log.info("Обработка существующих файлов...");
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(inputDirectory)) {
            for (Path entry : stream) {
                if (Files.isRegularFile(entry)) {
                    processFileAsync(entry);
                }
            }
        } catch (IOException e) {
            log.error("Не удалось получить доступ к каталогу: {}", inputDirectory, e);
        }
    }

    public void startDirectoryWatch() {
        log.info("Начинаю смотреть каталог...");
        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
            inputDirectory.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
            log.info("Наблюдатель каталогов зарегистрирован для: {}", inputDirectory);

            executorService.submit(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    WatchKey key;
                    try {
                        key = watchService.poll(1, TimeUnit.SECONDS);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.warn("Служба наблюдения прервана", e);
                        break;
                    }

                    if (key != null) {
                        for (WatchEvent<?> event : key.pollEvents()) {
                            WatchEvent.Kind<?> kind = event.kind();
                            if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                                Path filePath = inputDirectory.resolve((Path) event.context());
                                log.info("Обнаружен новый файл: {}", filePath);
                                processFileAsync(filePath);
                            }
                        }
                        key.reset();
                    }
                }
            });
        } catch (IOException e) {
            log.error("Не удалось настроить наблюдатель каталогов.: {}", e.getMessage());
            throw new RuntimeException("Не удалось настроить наблюдатель каталогов для: " + inputDirectory, e);
        }
    }

    void processFileAsync(Path filePath) {
        synchronized (processedFiles) {
            if (!processedFiles.contains(filePath)) {
                executorService.submit(() -> {
                    try {
                        log.info("Обработка файла: {}", filePath);
                        parserService.parseFile(filePath);
                        processedFiles.add(filePath);
                    } catch (FileProcessingException e) {
                        log.error("Ошибка обработки файла: {}", filePath, e);
                    }
                });
            }
        }
    }

    @PreDestroy
    public void shutdown() {
        log.info("Выключение наблюдателя каталогов...");
        try {
            executorService.shutdown();
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                log.warn("Принудительное завершение службы исполнителя.");
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.error("Ошибка во время выключения: ", e);
            executorService.shutdownNow();
        }
    }
}
