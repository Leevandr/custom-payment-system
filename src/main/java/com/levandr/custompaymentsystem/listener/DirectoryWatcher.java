package com.levandr.custompaymentsystem.listener;

import com.levandr.custompaymentsystem.service.parser.FileParser;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;

@Component
@RequiredArgsConstructor
public class DirectoryWatcher {

    private final Path inputDirectory = Paths.get("src/main/resources/Input");
    private final FileParser fileParser;

    @PostConstruct
    public void init() {
        Thread watcherThread = new Thread(this::watchDirectory);
        watcherThread.setDaemon(true);
        watcherThread.start();
    }

    public void watchDirectory() {
        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
            inputDirectory.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);

            WatchKey key;
            while ((key = watchService.take()) != null) {
                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();

                    if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                        Path filePath = inputDirectory.resolve((Path) event.context());
                        System.out.println("File created: " + filePath);

                        fileParser.parseFile(filePath);
                    }
                }
                key.reset();
            }


        } catch (IOException | InterruptedException exception) {
            exception.printStackTrace();

        }
    }

}
