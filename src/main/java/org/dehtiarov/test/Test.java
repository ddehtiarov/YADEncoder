package org.dehtiarov.test;

import com.yandex.disk.rest.Credentials;
import com.yandex.disk.rest.RestClient;
import org.dehtiarov.User.User;
import org.dehtiarov.service.DiskService;
import org.dehtiarov.service.impl.YADiskService;
import org.dehtiarov.util.DirectoryWatcherRunnable;

import java.nio.file.Paths;

public class Test {

    public static void main(String[] args) {

        String user = "e5fe2b763a124e97aff1bd54a782f1b0";
        String token = "ARR-oJQAAyCw6JVCc8QGRlOSi9-oVuFY0A";

        Credentials credentials = new Credentials(user, token);
        RestClient restClient = new RestClient(credentials);

//        User user1 = new User();
//        user1.setKey("dimadima");
//        user1.setLocalFolderPath("/home/dehtiarov/IdeaProjects/");
//        user1.setRemoteFolderPath("yad-BPID");
//        user1.setLocalUploadFolder("/home/dehtiarov/IdeaProjects/yad-BPID-local");
//        user1.setDataEncoded(true);
//
//        DiskService diskService = new YADiskService(restClient, user1);
//
//        diskService.upload();
//        diskService.download("", "");
//        FileUtil.watchDirectoryPath(Paths.get(user1.getLocalUploadFolder()), diskService);

//        startInUploadMode(restClient);
        startInDownloadMode(restClient);
    }

    private static void startInUploadMode(RestClient restClient){
        User user = getUserInUploadMode();
        DiskService diskService = new YADiskService(restClient, user);
        diskService.upload();

        startWatchingDirectory(user, diskService);
        //FileUtil.watchDirectoryPath(Paths.get(user.getLocalUploadFolder()), diskService);
    }

    private static void startWatchingDirectory(User user, DiskService diskService) {
        DirectoryWatcherRunnable directoryWatcherRunnable = new DirectoryWatcherRunnable();
        directoryWatcherRunnable.setDiskService(diskService);
        directoryWatcherRunnable.setPath(Paths.get(user.getLocalUploadFolder()));
        Thread thread = new Thread(directoryWatcherRunnable);
        thread.start();
    }

    private static void startInDownloadMode(RestClient restClient){
        User user = getUserInDownloadMode();
        DiskService diskService = new YADiskService(restClient, user);
        diskService.download("", "");

        startWatchingDirectory(user, diskService);
        //FileUtil.watchDirectoryPath(Paths.get(user.getLocalUploadFolder()), diskService);
    }

    private static User getUserInDownloadMode(){
        User user = new User();
        user.setKey("dimadima");
        user.setLocalFolderPath("/home/dehtiarov/IdeaProjects/");
        user.setRemoteFolderPath("yad-BPID");
        user.setLocalUploadFolder("/home/dehtiarov/IdeaProjects/yad-BPID");
        user.setDataEncoded(true);
        return user;
    }

    private static User getUserInUploadMode(){
        User user = new User();
        user.setKey("dimadima");
        String homePath = "/home/dehtiarov/IdeaProjects/";
        user.setLocalFolderPath("/home/dehtiarov/IdeaProjects/");
        user.setRemoteFolderPath("yad-BPID-local");
        user.setLocalUploadFolder("/home/dehtiarov/IdeaProjects/yad-BPID-local");
        user.setDataEncoded(true);
        return user;
    }

}
