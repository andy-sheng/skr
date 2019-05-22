package com.module.playways.grab.room.model;

import com.module.playways.room.song.model.SongModel;

import java.io.Serializable;

public class WorksUploadModel implements Serializable {
    String localPath;// 本地路径
    SongModel mSongModel;
    boolean isBlight; // 是否是爆灯时刻
    int worksID; // 用来标记是否已经上传了
    String url;  // 记录上传的路径

    public WorksUploadModel(String localPath, SongModel songModel, boolean isBlight) {
        this.localPath = localPath;
        this.mSongModel = songModel;
        this.isBlight = isBlight;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public SongModel getSongModel() {
        return mSongModel;
    }

    public void setSongModel(SongModel songModel) {
        mSongModel = songModel;
    }

    public boolean isBlight() {
        return isBlight;
    }

    public void setBlight(boolean blight) {
        isBlight = blight;
    }


    public int getWorksID() {
        return worksID;
    }

    public void setWorksID(int worksID) {
        this.worksID = worksID;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "WonderfulMomentModel{" +
                "localPath='" + localPath + '\'' +
                ", mSongModel=" + mSongModel +
                ", isBlight=" + isBlight +
                ", worksID=" + worksID +
                ", url='" + url + '\'' +
                '}';
    }
}
