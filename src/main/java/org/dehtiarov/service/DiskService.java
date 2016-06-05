package org.dehtiarov.service;

import com.yandex.disk.rest.exceptions.ServerException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public interface DiskService {

    void upload(String path);

    void upload();

    void download(String localPath, String remotePath);

    void uploadFile(Path filePath, String folderPath) throws IOException, ServerException;

}
