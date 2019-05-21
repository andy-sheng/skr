package com.module.playways.grab.room.model;

import com.module.playways.room.song.model.SongModel;

import java.io.Serializable;

public class WonderfulMomentModel implements Serializable {
    String localPath;
    SongModel mSongModel;
    boolean isBlight;

    public WonderfulMomentModel(String localPath, SongModel songModel, boolean isBlight) {
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
}
