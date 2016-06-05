package org.dehtiarov.util;

import org.dehtiarov.service.DiskService;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.WatchEvent.Kind;

public class FileUtil {

    public static void unZip(String zipFile, String outputFolder) {
        byte[] buffer = new byte[1024];
        try {
            File folder = new File(outputFolder);
            if (!folder.exists()) {
                folder.mkdir();
            }
            ZipInputStream zis =
                    new ZipInputStream(new FileInputStream(zipFile));
            ZipEntry ze = zis.getNextEntry();
            while (ze != null) {
                String fileName = ze.getName();
                File newFile = new File(outputFolder + File.separator + fileName);
                System.out.println("file unzip : " + newFile.getAbsoluteFile());
                new File(newFile.getParent()).mkdirs();
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
                ze = zis.getNextEntry();
            }
            zis.closeEntry();
            zis.close();
            System.out.println("Done");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static boolean removeFile(String path) {
        File file = new File(path);
        return file.delete();
    }

    public static boolean removeDirectory(String path) {
        File file = new File(path);
        return removeDirectory(file);
    }

    private static boolean removeDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (null != files) {
                for (int i = 0; i < files.length; i++) {
                    if (files[i].isDirectory()) {
                        removeDirectory(files[i]);
                    } else {
                        files[i].delete();
                    }
                }
            }
        }
        return (directory.delete());
    }

    public static void copyFile(File src, File dest)
            throws IOException {
        copyFile(src, dest, null);
    }

    public static void copyFile(File src, File dest, String key)
            throws IOException {
        if (src.isDirectory()) {
            if (!dest.exists()) {
                dest.mkdir();
                System.out.println("Directory copied from " + src + "  to " + dest);
            }
            String files[] = src.list();
            for (String file : files) {
                File srcFile = new File(src, file);
                File destFile = new File(dest, file);
                copyFile(srcFile, destFile, key);
            }

        } else {
            if (Objects.isNull(key)) {
                copySimple(src, dest);
            } else {
                copyAndScramble(src, dest, key);
            }
            System.out.println("File copied from " + src + " to " + dest);
        }
    }

    private static void copyAndScramble(File src, File dest, String key) throws FileNotFoundException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dest);
        try {
            ScramblerUtil.decrypt(key, in, out);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    private static void copySimple(File src, File dest) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dest);

        byte[] buffer = new byte[1024];

        int length;

        while ((length = in.read(buffer)) > 0) {
            out.write(buffer, 0, length);
        }

        in.close();
        out.close();
    }

    public static void watchDirectoryPath(Path path, DiskService diskService) {
        System.out.println("Ready for watching path: " + path);
        FileSystem fs = path.getFileSystem();
        try (WatchService service = fs.newWatchService()) {
            path.register(service, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
            registerRecursive(path, service);
            WatchKey key;
            while (true) {
                key = service.take();
                Kind<?> kind;
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
            watchDirectoryPath(path, diskService);
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

    public static boolean isDirectory(Path path) {
        try {
            return (Boolean) Files.getAttribute(path,
                    "basic:isDirectory", NOFOLLOW_LINKS);
        } finally {
            return false;
        }
    }
}
