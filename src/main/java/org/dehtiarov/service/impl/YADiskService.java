package org.dehtiarov.service.impl;

import com.yandex.disk.rest.DownloadListener;
import com.yandex.disk.rest.ProgressListener;
import com.yandex.disk.rest.RestClient;
import com.yandex.disk.rest.exceptions.ServerException;
import com.yandex.disk.rest.json.Link;
import org.dehtiarov.User.User;
import org.dehtiarov.service.DiskService;
import org.dehtiarov.util.FileUtil;
import org.dehtiarov.util.ScramblerUtil;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class YADiskService implements DiskService {

    private static final String ZIP_FORMAT = ".zip";

    private String zipFilePath;

    private RestClient restClient;

    private User user;

    public YADiskService(RestClient restClient, User user) {
        this.restClient = restClient;
        this.user = user;
        this.zipFilePath = user.getLocalFolderPath() + user.getRemoteFolderPath() + ZIP_FORMAT;
    }

    @Override
    public void upload() {
        upload(user.getLocalUploadFolder());
    }

    @Override
    public void upload(String uploadFolderPath) {
        try {
            if (FileUtil.isDirectory(Paths.get(uploadFolderPath))) {
                restClient.makeFolder(user.getRemoteFolderPath());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            Files.walk(Paths.get(uploadFolderPath)).forEach(filePath -> {
                try {
                    String folderPath = user.getRemoteFolderPath() + filePath
                            .toAbsolutePath()
                            .toString()
                            .replace(uploadFolderPath, "");

                    if (Files.isRegularFile(filePath)) {
                        uploadFile(filePath, folderPath);
                    } else {
                        restClient.makeFolder(folderPath);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void uploadFile(Path filePath, String folderPath) throws IOException, ServerException {
        ProgressListener progressListener = new ProgressListener() {
            public void updateProgress(long l, long l1) {
            }

            public boolean hasCancelled() {
                return false;
            }
        };

        Link link = restClient.getUploadLink(folderPath, true);
        if (user.isDataEncoded()) {
            uploadEncodedFile(progressListener, filePath, link);
        } else {
            restClient.uploadFile(link, true, new File(filePath.toAbsolutePath().toString()), progressListener);
        }
    }

    private void uploadEncodedFile(ProgressListener progressListener, Path filePath, Link link) throws IOException, ServerException {
        String tempFilePath = filePath.toAbsolutePath().toString() + "-local";

        File file = new File(tempFilePath);

        FileInputStream fis = new FileInputStream(filePath.toAbsolutePath().toString());
        FileOutputStream fos = new FileOutputStream(file);

        try {
            ScramblerUtil.encrypt(user.getKey(), fis, fos);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        restClient.uploadFile(link, true, file, progressListener);

        file.delete();
    }

    @Override
    public void download(String localPath, String remotePath) {
        downloadDiskData();

        String tempFolder = user.getLocalFolderPath() + user.getRemoteFolderPath() + "-disk";

        FileUtil.unZip(zipFilePath, tempFolder);
        try {
            if (!user.isDataEncoded()) {
                FileUtil.copyFile(new File(tempFolder), new File(user.getLocalFolderPath()), user.getKey());
            } else {
                FileUtil.copyFile(new File(tempFolder), new File(user.getLocalFolderPath()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        FileUtil.removeFile(zipFilePath);
        FileUtil.removeDirectory(tempFolder);
    }

    private void downloadDiskData() {
        DownloadListener downloadListener = new DownloadListener() {
            @Override
            public OutputStream getOutputStream(boolean b) throws IOException {
                File file = new File(zipFilePath);
                file.createNewFile();
                return new BufferedOutputStream(new FileOutputStream(file));
            }
        };

        try {
            restClient.downloadFile(user.getRemoteFolderPath(), downloadListener);
        } catch (IOException | ServerException e) {
            e.printStackTrace();
        }
    }
}
