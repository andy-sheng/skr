package com.mi.live.data.repository.DataType;

/**
 * Created by zhangyuehuan on 15-12-24.
 */
public class PhotoFolder {
    private String photoPath;
    private String folderName;
    private String folderID;
    private int photoCnt;

    public PhotoFolder() {
    }

    public PhotoFolder(String photoPath, String folderName, int photoCnt) {
        this.photoPath = photoPath;
        this.folderName = folderName;
        this.photoCnt = photoCnt;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public int getPhotoCnt() {
        return photoCnt;
    }

    public void setPhotoCnt(int photoCnt) {
        this.photoCnt = photoCnt;
    }

    public String getFolderID() {
        return folderID;
    }

    public void setFolderID(String folderID) {
        this.folderID = folderID;
    }
}