package org.dehtiarov.User;

/**
 * Created by dehtiarov on 5/18/16.
 */
public class User {
    boolean isDataEncoded;

    String localUploadFolder;

    String key;

    String localFolderPath;

    String remoteFolderPath;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getLocalFolderPath() {
        return localFolderPath;
    }

    public void setLocalFolderPath(String localFolderPath) {
        this.localFolderPath = localFolderPath;
    }

    public String getRemoteFolderPath() {
        return remoteFolderPath;
    }

    public void setRemoteFolderPath(String remoteFolderPath) {
        this.remoteFolderPath = remoteFolderPath;
    }

    public String getLocalUploadFolder() {
        return localUploadFolder;
    }

    public void setLocalUploadFolder(String localUploadFolder) {
        this.localUploadFolder = localUploadFolder;
    }

    public boolean isDataEncoded() {
        return isDataEncoded;
    }

    public void setDataEncoded(boolean dataEncoded) {
        isDataEncoded = dataEncoded;
    }
}
