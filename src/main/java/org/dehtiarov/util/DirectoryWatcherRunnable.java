package org.dehtiarov.util;

import org.dehtiarov.service.DiskService;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

import static java.nio.file.StandardWatchEventKinds.*;

public class DirectoryWatcherRunnable implements Runnable {

    private Path path;
    private DiskService diskService;

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public DiskService getDiskService() {
        return diskService;
    }

    public void setDiskService(DiskService diskService) {
        this.diskService = diskService;
    }

    public void watchDirectoryPath() {
        System.out.println("Ready for watching path: " + path);
        FileSystem fs = path.getFileSystem();
        try (WatchService service = fs.newWatchService()) {
            path.register(service, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
            registerRecursive(path, service);
            WatchKey key;
            while (true) {
                key = service.take();
                WatchEvent.Kind<?> kind;
                for (WatchEvent<?> watchEvent : key.pollEvents()) {
                    kind = watchEvent.kind();
                    if (OVERFLOW == kind) {
                        continue;
                    } else if (ENTRY_CREATE == kind) {
                        Path newPath = ((WatchEvent<Path>) watchEvent)
                                .context();
                        System.out.println("New path created: " + newPath);
                        diskService.upload();
                        key.cancel();
                    } else if (ENTRY_MODIFY == kind) {
                        Path newPath = ((WatchEvent<Path>) watchEvent)
                                .context();
                        System.out.println("New path modified: " + newPath);
                        diskService.upload();
                        key.cancel();
                    } else if (ENTRY_DELETE == kind) {
                        Path newPath = ((WatchEvent<Path>) watchEvent)
                                .context();
                        System.out.println("file deleted: " + newPath);
                        diskService.upload();
                        key.cancel();
                    }
                }
                if (!key.reset()) {
                    break;
                }
            }
            watchDirectoryPath();
        } catch (Exception ioe) {
            ioe.printStackTrace();
        }
    }

    private static void registerRecursive(final Path root, final WatchService watchService) throws IOException {
        Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                dir.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    @Override
    public void run() {
        watchDirectoryPath();
    }
}
