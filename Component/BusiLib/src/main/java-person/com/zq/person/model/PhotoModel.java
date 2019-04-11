package com.zq.person.model;

import com.zq.person.photo.PhotoModelDB;

import java.io.Serializable;

public class PhotoModel implements Serializable {
    public static final int STATUS_SUCCESS = 0;// 上传成功
    public static final int STATUS_WAIT_UPLOAD = 1;// 等待上传
    public static final int STATUS_UPLOADING = 2;// 上传中
    public static final int STATUS_FAILED = 3;// 上传失败
    public static final int STATUS_DELETE = 4;// 删除了
    /**
     * picID : 0
     * picPath : string
     */
    private int picID;
    private String picPath;

    private String localPath;// 本地路径
    private int status = STATUS_SUCCESS;

    public static PhotoModelDB toPhotoDB(PhotoModel pm) {
        PhotoModelDB photoModelDB = new PhotoModelDB();
        photoModelDB.setLocalPath(pm.getLocalPath());
        photoModelDB.setStatus(pm.getStatus());
        return photoModelDB;
    }

    public static PhotoModel fromDB(PhotoModelDB photoModelDB) {
        PhotoModel pm = new PhotoModel();
        pm.setLocalPath(photoModelDB.getLocalPath());
        pm.setStatus(photoModelDB.getStatus());
        return pm;
    }

    public int getPicID() {
        return picID;
    }

    public void setPicID(int picID) {
        this.picID = picID;
    }

    public String getPicPath() {
        return picPath;
    }

    public void setPicPath(String picPath) {
        this.picPath = picPath;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PhotoModel that = (PhotoModel) o;
        if (status == STATUS_SUCCESS && that.status == STATUS_SUCCESS) {
            // 都上传成功了 ID path 有一个一样就认为同一张是同一张照片
            if (picPath != null && picPath.equals(that.picPath)) {
                return true;
            }
            if (picID == that.picID) {
                return true;
            }
        } else {
            // 只要有一个上传成功，就比本地的
            if (localPath != null && localPath.equals(that.localPath)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = picID;
        result = 31 * result + (picPath != null ? picPath.hashCode() : 0);
        result = 31 * result + (localPath != null ? localPath.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "PhotoModel{" +
                "picID=" + picID +
                ", picPath='" + picPath + '\'' +
                ", localPath='" + localPath + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
