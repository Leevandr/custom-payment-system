package com.levandr.custompaymentsystem.listener;

import com.levandr.custompaymentsystem.service.parser.FileParser;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;
import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DirectoryWatcher {

    private final Path inputDirectory = Paths.get("src/main/resources/Input");
    private final FileParser fileParser;
    private final Set<Path> processedFiles = new HashSet<>();


    public void init() {
        watchDirectory();
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
                        synchronized (processedFiles) {
                            if (!processedFiles.contains(filePath)) {
                                System.out.println("File created: " + filePath);

                                fileParser.parseFile(filePath);
                                processedFiles.add(filePath);
                            }
                        }

                    }
                }
                key.reset();
            }


        } catch (IOException | InterruptedException exception) {
            exception.printStackTrace();

        }
    }

}
